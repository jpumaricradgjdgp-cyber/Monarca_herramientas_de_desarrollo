function renderizarResumen() {
    document.getElementById('checkout-items').innerHTML = carrito.map(item => `<div class="check-item">
        <div class="check-img-contenedor">${imagenProductoHTML(item)}<span class="check-cantidad">${item.cantidad}</span></div>
        <div class="check-info"><h4>${escaparHTML(item.nombre)}</h4><p>${escaparHTML(item.talla)} / ${escaparHTML(item.color)}</p></div>
        <div class="check-precio">S/ ${(item.precio * item.cantidad).toFixed(2)}</div></div>`).join('') || '<p>Tu carrito está vacío.</p>';
    const subtotal = carrito.reduce((suma, item) => suma + item.precio * item.cantidad, 0);
    document.getElementById('check-subtotal').textContent = `S/ ${subtotal.toFixed(2)}`;
    document.getElementById('check-total').textContent = `S/ ${subtotal.toFixed(2)}`;
    document.getElementById('check-envio').textContent = 'S/ 0.00';
    document.getElementById('fila-descuento').style.display = 'none';
}
document.addEventListener('DOMContentLoaded', async () => {
    const form = document.getElementById('form-checkout');
    const boton = form.querySelector('button[type="submit"]');
    const pagos = document.querySelector('.opciones-pago');
    boton.disabled = true;
    if (!localStorage.getItem('token_monarca')) { location.href = paginaTienda('login.html'); return; }
    let enviando = false;
    form.addEventListener('submit', async e => {
        e.preventDefault();
        if (enviando || boton.disabled || !form.reportValidity()) return;
        const token = localStorage.getItem('token_monarca');
        if (!token) { location.href = paginaTienda('login.html'); return; }
        enviando = true; boton.disabled = true;
        try {
            const cambiado = await sincronizarCarrito();
            renderizarResumen();
            if (!carrito.length) throw new Error('Tu carrito está vacío o los artículos ya no tienen stock.');
            if (cambiado) throw new Error('Actualizamos precios o cantidades. Revisa el resumen y confirma nuevamente.');
            const seleccionado = form.querySelector('input[name="pago"]:checked');
            if (!seleccionado) throw new Error('Selecciona un método de pago.');
            const datosEntrega = ['nombre','apellidos','correo','telefono','dni','direccion','distrito','referencia']
                .map(id => `${id}: ${document.getElementById(id).value.trim()}`).join('\n');
            if (datosEntrega.length > 500) throw new Error('Los datos de entrega son demasiado largos (máximo 500 caracteres en total).');
            const payload = {
                idMetodoPago: Number(seleccionado.value), observacion: datosEntrega,
                items: carrito.map(({idVariante, cantidad}) => ({idVariante, cantidad}))
            };
            const respuesta = await fetch(`${TIENDA_API}/pedidos/procesar`, {
                method: 'POST', headers: {'Content-Type':'application/json', Authorization:`Bearer ${token}`},
                body: JSON.stringify(payload)
            });
            if (respuesta.status === 401 || respuesta.status === 403) throw new Error('Tu sesión no permite confirmar el pedido. Vuelve a iniciar sesión.');
            const resultado = await respuesta.json();
            if (!respuesta.ok) throw new Error(resultado.error || 'No se pudo registrar el pedido.');
            carrito = []; localStorage.removeItem('monarca_carrito');
            alert(`Pedido ${resultado.codigoPedido} registrado. Total: S/ ${Number(resultado.total).toFixed(2)}. Estado: ${resultado.estado}. El pago está pendiente.`);
            location.href = new URL('index.html', TIENDA_RAIZ).href;
        } catch (error) { alert(error.message); }
        finally { enviando = false; boton.disabled = !carrito.length; }
    });
    try {
        const [cambiado, metodos] = await Promise.all([sincronizarCarrito(), obtenerTienda('/metodos-pago', true)]);
        renderizarResumen();
        pagos.innerHTML = metodos.map(m => `<label class="opcion-radio"><input type="radio" name="pago" value="${m.idMetodoPago}" required>
            <span>${escaparHTML(m.nombre)}</span></label>`).join('') || '<p>No hay métodos de pago disponibles.</p>';
        if (cambiado) alert('Actualizamos el carrito con los precios y el stock disponibles. Revisa el resumen.');
        boton.disabled = !carrito.length || !metodos.length;
    } catch (error) { pagos.textContent = error.message + ' Recarga la página para reintentar.'; }
});
