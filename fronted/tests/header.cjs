const {chromium} = require('playwright');
const fs = require('node:fs');
const path = require('node:path');
const http = require('node:http');
const assert = require('node:assert/strict');
(async()=>{
 const root=path.resolve(__dirname, '..');
 const server=http.createServer((req,res)=>{
  const file=path.resolve(root,'.'+decodeURIComponent(new URL(req.url,'http://localhost').pathname));
  if(!file.startsWith(path.resolve(root)+path.sep)) return res.writeHead(403).end();
  fs.readFile(file,(e,data)=>{if(e)return res.writeHead(404).end();res.setHeader('Content-Type',{'.html':'text/html; charset=utf-8','.css':'text/css','.js':'text/javascript'}[path.extname(file)]||'application/octet-stream');res.end(data);});
 });
 await new Promise(r=>server.listen(0,'127.0.0.1',r));
 const browser=await chromium.launch({headless:true,...(process.env.BROWSER_EXECUTABLE ? {executablePath:process.env.BROWSER_EXECUTABLE} : {})});
 try {
  const page=await browser.newPage();
  await page.route('https://**',r=>r.abort());
  await page.route('http://localhost:8080/**',r=>r.fulfill({contentType:'application/json',body:'[]'}));
  // Aislar encabezados de las redirecciones de checkout/detalle sin datos.
  await page.route('**/js/checkout.js',r=>r.abort());
  await page.route('**/js/producto.js',r=>r.abort());
  const pages=['index.html',...['Tops','Bottoms','Calzado','Accesorios','Contacto','Nosotros','producto','checkout'].map(n=>'Paginas/'+n+'.html')];
  for(const logged of [false,true]) {
   await page.addInitScript(logged=>{
    if(logged) localStorage.setItem('token_monarca','test.'+btoa(JSON.stringify({sub:'nombre-muy-largo-del-cliente@example.com'}))+'.test');
    else localStorage.removeItem('token_monarca');
   },logged);
   for(const file of pages) {
    await page.goto(`http://127.0.0.1:${server.address().port}/${file}`,{waitUntil:'domcontentloaded'});
    await page.locator('.header .logo img').evaluate(img=>img.decode());
    for(const width of [320,390,768,900]) {
     await page.setViewportSize({width,height:800});
     const header=page.locator('.header');
     const stats=await header.evaluate(el=>({h:el.getBoundingClientRect().height,scroll:el.scrollWidth,client:el.clientWidth,pos:getComputedStyle(el).position,bg:getComputedStyle(el).backgroundColor}));
     assert.equal(stats.h,64,file);
     assert(stats.scroll<=stats.client+1,`${file}: overflow ${width}`);
     assert.equal(stats.pos,'fixed');
     const nav=page.locator('.header .navbar');
     assert.equal(await nav.isVisible(),false);
     const toggle=page.locator('#menu-toggle');
     await toggle.focus();await page.keyboard.press('Space');
     assert.equal(await nav.isVisible(),true);
     await page.keyboard.press('Tab');
     assert.equal(await nav.locator('a').first().evaluate(el=>el===document.activeElement),true);
     await toggle.focus();await page.keyboard.press('Space');
     assert.equal(await nav.isVisible(),false);
     const rects=await page.locator('.header .menu-icon, .header .logo, .header .iconos-usuario').evaluateAll(els=>els.map(el=>{const r=el.getBoundingClientRect();return {left:r.left,right:r.right,top:r.top,bottom:r.bottom};}).sort((a,b)=>a.left-b.left));
     rects.forEach((r,i)=>{assert(r.left>=0&&r.right<=width+1,file+' bounds');if(i)assert(rects[i-1].right<=r.left+1,file+' overlap');});
    }

    await page.setViewportSize({width:1440,height:900});
    assert.equal(await page.locator('.header .navbar').isVisible(),true);
    assert.equal(await page.locator('.header .menu-icon').isVisible(),false);
   }
  }
  console.log('PASS: 9 paginas, visitante y sesion simulada; barra de 64px sin solapamientos a 320/390/768/900; menu con teclado; navegacion de escritorio a 1440. API simulada; CDN bloqueados.');
 } finally {await browser.close();await new Promise(r=>server.close(r));}
})().catch(e=>{console.error(e);process.exitCode=1;});
