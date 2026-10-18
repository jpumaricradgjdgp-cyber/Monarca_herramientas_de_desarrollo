const {chromium}=require('playwright');
const fs=require('node:fs'),path=require('node:path'),http=require('node:http'),assert=require('node:assert/strict');
(async()=>{
 const root=path.resolve(__dirname,'..');
 const server=http.createServer((req,res)=>{
  const file=path.resolve(root,'.'+new URL(req.url,'http://localhost').pathname);
  if(!file.startsWith(root+path.sep))return res.writeHead(403).end();
  fs.readFile(file,(err,data)=>{if(err)return res.writeHead(404).end();res.setHeader('Content-Type',{'.html':'text/html; charset=utf-8','.js':'text/javascript','.css':'text/css'}[path.extname(file)]||'application/octet-stream');res.end(data);});
 });
 await new Promise(r=>server.listen(0,'127.0.0.1',r));
 const browser=await chromium.launch({headless:true,...(process.env.BROWSER_EXECUTABLE?{executablePath:process.env.BROWSER_EXECUTABLE}:{})});
 try{
  const page=await browser.newPage();const errors=[],requests=[];let modo='conflicto',cancelar=false,liberar;
  const pedidos=[{idPedido:1,cliente:'<img src=x onerror=alert(1)>',total:'130.00',estado:'PENDIENTE_PAGO'},
    {idPedido:2,cliente:'Cliente',total:20,estado:'PENDIENTE_PAGO'},
    {idPedido:3,cliente:'Cliente',total:20,estado:'PAGADO'},
    {idPedido:4,cliente:'Cliente',total:20,estado:'ENTREGADO'}];
  page.on('pageerror',e=>errors.push(e.message));
  page.on('dialog',async d=>{if(d.type()==='confirm'&&cancelar)await d.accept();else await d.dismiss();});
  await page.route('https://**',r=>r.abort());
  await page.addInitScript(()=>localStorage.setItem('token_monarca','fixture.'+btoa(JSON.stringify({sub:'admin@example.test'}))+'.fixture'));
  await page.route('http://localhost:8080/api/**',async route=>{
   const req=route.request(),url=new URL(req.url());
   if(req.method()==='PUT'){
    const datos=req.postDataJSON();requests.push({url:url.pathname,datos});
    if(modo==='conflicto')return route.fulfill({status:409,contentType:'application/json',body:JSON.stringify({error:'El pedido ya cambió. Actualiza la lista.'})});
    if(modo==='espera')await new Promise(r=>liberar=r);
    const id=Number(url.pathname.split('/')[3]);pedidos.find(p=>p.idPedido===id).estado=datos.estado;
    return route.fulfill({contentType:'application/json',body:JSON.stringify({idPedido:id,estado:datos.estado})});
   }
   if(url.pathname==='/api/pedidos'&&modo==='sinpermiso')return route.fulfill({status:403,body:'{}'});
   // Simula una recarga lenta de la tabla para detectar lecturas del formulario anterior.
   if(url.pathname==='/api/pedidos')await new Promise(r=>setTimeout(r,150));
   const data=url.pathname==='/api/pedidos'?pedidos:[];
   await route.fulfill({contentType:'application/json',body:JSON.stringify(data)});
  });
  const url=`http://127.0.0.1:${server.address().port}/Paginas/admin.html`;
  await page.goto(url);const form=()=>page.locator('form[data-pedido="1"]');
  await form().waitFor();
  assert.equal(await page.locator('#tabla-ventas img').count(),0);
  assert.match(await page.locator('#tabla-ventas').textContent(),/S\/ 130.00/);
  await form().getByRole('button',{name:'Guardar estado'}).click();assert.equal(requests.length,0);
  await form().locator('[name="referenciaPago"]').fill('   ');
  await form().getByRole('button',{name:'Guardar estado'}).click();assert.equal(requests.length,0);
  await form().locator('[name="referenciaPago"]').fill(' YAPE-123 ');
  await form().getByRole('button',{name:'Guardar estado'}).click();
  await form().getByText('El pedido ya cambió. Actualiza la lista.').waitFor();
  assert.equal(await form().locator('[name="referenciaPago"]').inputValue(),' YAPE-123 ');
  assert.equal(await form().getByRole('button').isEnabled(),true);
  modo='espera';await form().getByRole('button').click();
  await page.waitForFunction(()=>document.querySelector('form[data-pedido="1"] button').disabled);
  while(!liberar)await new Promise(r=>setTimeout(r,10));
  await form().evaluate(f=>f.requestSubmit());assert.equal(requests.length,2);
  liberar();await page.locator('#mensaje-pedidos').getByText('Pedido #1 actualizado a PAGADO.').waitFor();
  assert.deepEqual(requests[1].datos,{estado:'PAGADO',referenciaPago:'YAPE-123'});
  // El mensaje se muestra antes del GET: esperar al formulario nuevo, no solo al aviso.
  await page.waitForFunction(()=>document.querySelector('form[data-pedido="1"] [name="estado"]')?.value==='CONFIRMADO');
  assert.equal(await form().locator('[name="estado"]').inputValue(),'CONFIRMADO');
  assert.equal(await form().locator('.referencia-pedido').isVisible(),false);
  modo='normal';await form().locator('[name="estado"]').selectOption('ENVIADO');
  await form().getByRole('button').click();await page.locator('#mensaje-pedidos').getByText('Pedido #1 actualizado a ENVIADO.').waitFor();
  await page.waitForFunction(()=>document.querySelector('form[data-pedido="1"] [name="estado"]')?.value==='ENTREGADO');
  assert.equal(await form().locator('[name="estado"]').inputValue(),'ENTREGADO');
  const cancelarForm=page.locator('form[data-pedido="2"]');await cancelarForm.locator('select').selectOption('CANCELADO');
  assert.equal(await cancelarForm.locator('[name="referenciaPago"]').isDisabled(),true);
  const anteriores=requests.length;await cancelarForm.getByRole('button').click();assert.equal(requests.length,anteriores);
  cancelar=true;await cancelarForm.getByRole('button').click();
  await page.locator('#mensaje-pedidos').getByText('Pedido #2 actualizado a CANCELADO.').waitFor();
  await page.locator('form[data-pedido="2"]').waitFor({state:'detached'});
  assert.equal(await page.locator('form[data-pedido="2"]').count(),0);
  assert.equal(await page.locator('form[data-pedido="4"]').count(),0);
  assert.deepEqual(requests.at(-1).datos,{estado:'CANCELADO',referenciaPago:null});
  await page.setViewportSize({width:390,height:844});
  assert.equal(await page.locator('.pedidos-scroll').evaluate(el=>getComputedStyle(el).overflowX),'auto');
  modo='sinpermiso';await page.reload();await page.getByText('No tienes permisos de administrador o tu sesión ha vencido.').waitFor();
  assert.equal(await page.locator('.pedido-form').count(),0);assert.deepEqual(errors,[]);
  console.log('PASS: administrador confirma con referencia, conserva campos ante 409, evita doble envío, avanza estados, confirma cancelación y maneja 403. API simulada.');
 }finally{await browser.close();await new Promise(r=>server.close(r));}
})().catch(e=>{console.error(e);process.exitCode=1;});
