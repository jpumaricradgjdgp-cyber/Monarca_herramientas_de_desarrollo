const {chromium} = require('playwright');
const fs = require('node:fs');
const path = require('node:path');
const http = require('node:http');
const assert = require('node:assert/strict');
(async () => {
 const root = path.resolve(__dirname, '..');
 const server = http.createServer((req,res) => {
  const file = path.resolve(root,'.'+decodeURIComponent(new URL(req.url,'http://localhost').pathname));
  if (!file.startsWith(path.resolve(root)+path.sep)) return res.writeHead(403).end();
  fs.readFile(file,(error,data) => {
   if (error) return res.writeHead(404).end();
   res.setHeader('Content-Type', {'.html':'text/html; charset=utf-8','.js':'text/javascript; charset=utf-8','.css':'text/css; charset=utf-8'}[path.extname(file)]||'application/octet-stream');
   res.end(data);
  });
 });
 await new Promise(resolve => server.listen(0,'127.0.0.1',resolve));
 const browser = await chromium.launch({headless:true,...(process.env.BROWSER_EXECUTABLE ? {executablePath:process.env.BROWSER_EXECUTABLE} : {})});
 try {
  for (const touch of [false,true]) {
   const context = await browser.newContext({hasTouch:touch});
   const page = await context.newPage();
   const errors = [];
   page.on('pageerror',e => errors.push(e.message));
   await page.route('https://**',r => r.abort());
   for (const category of ['Tops','Bottoms','Calzado','Accesorios']) {
    const products = [
     {idProducto:1,nombre:'Prenda de prueba',categoria:category,marca:'Monarca',precioBase:30,variantes:[{idVariante:1,talla:'S',color:'Negro',stock:4}]},
     {idProducto:2,nombre:'Otra prenda',categoria:category,marca:'Monarca',precioBase:500,variantes:[{idVariante:2,talla:'M',color:'Blanco',stock:3}]}
    ];
    await page.route('http://localhost:8080/**',r => r.fulfill({contentType:'application/json',body:JSON.stringify(products)}));
    await page.goto(`http://127.0.0.1:${server.address().port}/Paginas/${category}.html`);
    await page.locator('.producto-card').first().waitFor();
    assert.equal(await page.locator('.producto-card').count(),2);
    const buttons = page.locator('.filtro-item');
    assert.equal(await buttons.count(),4);
    for (let i=0;i<4;i++) {
     const button = buttons.nth(i);
     const id = await button.getAttribute('aria-controls');
     const panel = page.locator('#'+id);
     assert.equal(await panel.isVisible(),false);
     await button.focus();
     await page.keyboard.press('Enter');
     assert.equal(await button.getAttribute('aria-expanded'),'true');
     assert.equal(await panel.isVisible(),true);
     await page.keyboard.press('Tab');
     assert.equal(await panel.locator('input').first().evaluate(el=>document.activeElement===el),true);
     await button.focus();
     await page.keyboard.press('Space');
     assert.equal(await panel.isVisible(),false);
     await page.keyboard.press('Tab');
     assert.equal(await panel.evaluate(el=>el.contains(document.activeElement)),false);
    }
    await page.locator('#buscador-productos').fill('Otra');
    assert.equal(await page.locator('.producto-card').count(),1);
    await page.locator('#buscador-productos').fill('');
    await buttons.nth(3).click();
    await page.locator('#opciones-filtro-4 input[value="bajo"]').check();
    assert.equal(await page.locator('.producto-card').count(),1);
    await page.locator('#opciones-filtro-4 input[value="bajo"]').uncheck();
    for (const width of [390,768,1440]) {
     await page.setViewportSize({width,height:900});
     const dimensions = await page.locator('.sidebar-filtros').evaluate(el=>({width:el.getBoundingClientRect().width,scroll:el.scrollWidth,client:el.clientWidth}));
     if (dimensions.scroll > dimensions.client+1) console.log(await page.locator('.sidebar-filtros').evaluate(el=>({dimensions:{scroll:el.scrollWidth,client:el.clientWidth},children:[...el.querySelectorAll('*')].map(x=>({tag:x.tagName,cls:x.className,width:x.getBoundingClientRect().width,scroll:x.scrollWidth,position:getComputedStyle(x).position}))})));
     assert(dimensions.scroll <= dimensions.client+1, `${category}: desborde de filtros a ${width}`);
     assert(dimensions.width <= width, `${category}: ancho de filtros a ${width}`);
     if (touch) {
      const info = page.locator('.producto-hover-info').first();
      assert.equal(await info.evaluate(el=>getComputedStyle(el).opacity),'1');
      assert.equal(await info.evaluate(el=>getComputedStyle(el).maxHeight),'none');
     }
    }
    assert.deepEqual(errors,[]);
    await page.unroute('http://localhost:8080/**');
   }
   await context.close();
  }
  console.log('PASS: cuatro categorias; Enter/Espacio/Tab, opciones ocultas, busqueda y precio; filtros sin desborde a 390/768/1440; acciones visibles con tactil. API simulada y CDN bloqueados.');
 } finally {await browser.close();await new Promise(resolve=>server.close(resolve));}
})().catch(e=>{console.error(e);process.exitCode=1;});
