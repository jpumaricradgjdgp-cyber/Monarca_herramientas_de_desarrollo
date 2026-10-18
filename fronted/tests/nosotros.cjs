const { chromium } = require('playwright');
const fs = require('node:fs');
const path = require('node:path');
const http = require('node:http');
const assert = require('node:assert/strict');

(async () => {
  const root = path.resolve(__dirname, '..');
  const server = http.createServer((req, res) => {
    const file = path.resolve(root, '.' + decodeURIComponent(new URL(req.url, 'http://localhost').pathname));
    if (!file.startsWith(path.resolve(root) + path.sep)) return res.writeHead(403).end();
    fs.readFile(file, (error, data) => {
      if (error) return res.writeHead(404).end();
      res.setHeader('Content-Type', {'.html':'text/html; charset=utf-8','.css':'text/css; charset=utf-8','.js':'text/javascript; charset=utf-8'}[path.extname(file)] || 'application/octet-stream');
      res.end(data);
    });
  });
  await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
  const browser = await chromium.launch({headless:true, ...(process.env.BROWSER_EXECUTABLE ? {executablePath:process.env.BROWSER_EXECUTABLE} : {})});
  try {
    const page = await browser.newPage();
    const errors = [];
    page.on('pageerror', error => errors.push(error.message));
    await page.route('https://**', route => route.abort());
    await page.route('http://localhost:8080/**', route => route.fulfill({contentType:'application/json',body:'[]'}));
    await page.goto(`http://127.0.0.1:${server.address().port}/Paginas/Nosotros.html`, {waitUntil:'load'});
    const modal = page.locator('#modalMatriz');
    const opener = page.locator('#abrir-matriz');
    const closer = page.locator('#cerrar-matriz');
    assert.equal(await modal.isVisible(), false);
    await opener.focus();
    await page.keyboard.press('Enter');
    assert.equal(await modal.isVisible(), true);
    assert.equal(await closer.evaluate(el => document.activeElement === el), true);
    for (const key of ['Tab', 'Shift+Tab']) {
      await page.keyboard.press(key);
      assert.equal(await closer.evaluate(el => document.activeElement === el), true);
    }
    await page.keyboard.press('Escape');
    assert.equal(await modal.isVisible(), false);
    assert.equal(await opener.evaluate(el => document.activeElement === el), true);
    await opener.click();
    await closer.click();
    assert.equal(await modal.isVisible(), false);
    assert.equal(await opener.evaluate(el => document.activeElement === el), true);
    await opener.click();
    await modal.click({position:{x:2,y:2}});
    assert.equal(await modal.isVisible(), false);
    for (const width of [390, 768, 1440]) {
      await page.setViewportSize({width,height:900});
      await opener.click();
      assert.equal(await closer.isVisible(), true);
      await page.keyboard.press('Escape');
    }
    assert.equal(await page.locator('a[aria-current="page"]').textContent(), 'Nosotros');
    assert.deepEqual(errors, []);
    console.log('PASS: teclado, Escape, Tab/Shift+Tab, boton, fondo, foco, reapertura y tres anchos.');
  } finally {
    await browser.close();
    await new Promise(resolve => server.close(resolve));
  }
})().catch(error => {console.error(error);process.exitCode=1;});
