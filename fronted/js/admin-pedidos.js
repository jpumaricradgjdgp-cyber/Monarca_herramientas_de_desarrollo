// Gestión manual de pedidos. El backend vuelve a validar permiso y transición.
const transicionesPedido = {
    PENDIENTE_PAGO: ['PAGADO', 'CANCELADO'],
    PAGADO: ['CONFIRMADO', 'PREPARANDO', 'ENVIADO'],
    CONFIRMADO: ['PREPARANDO', 'ENVIADO'],
    PREPARANDO: ['ENVIADO'],
    ENVIADO: ['ENTREGADO']
};
const nombresEstadoPedido = {
    PAGADO: 'Confirmar pago', CONFIRMADO: 'Confirmar pedido', PREPARANDO: 'Preparar pedido',
    ENVIADO: 'Marcar enviado', ENTREGADO: 'Marcar entregado', CANCELADO: 'Cancelar pedido'
};

async function cargarVentas() {
    const tbody = document.getElementById('tabla-ventas');
    if (!tbody) return;
    try {
        const respuesta = await fetch(`${TIENDA_API}/pedidos`, {
            headers: {Authorization: `Bearer ${localStorage.getItem('token_monarca') || ''}`}
        });
        if (!respuesta.ok) throw new Error(respuesta.status === 403
            ? 'No tienes permisos de administrador o tu sesión ha vencido.' : 'No se pudieron cargar los pedidos.');
        const pedidos = await respuesta.json();
        tbody.innerHTML = pedidos.map(pedido => {
            const id = Number(pedido.idPedido);
            if (!Number.isSafeInteger(id) || id <= 0) return '';
            const opciones = transicionesPedido[pedido.estado] || [];
            const fecha = pedido.fechaPedido ? new Date(pedido.fechaPedido).toLocaleDateString() : 'Sin fecha';
            return `<tr><td>#${id}</td><td>${escaparHTML(fecha)}</td>
                <td>${escaparHTML(pedido.cliente || 'Cliente')}</td>
                <td>S/ ${Number(pedido.total || 0).toFixed(2)}</td>
                <td>${escaparHTML(pedido.estado)}</td><td>${opciones.length ? `
                    <form class="pedido-form" data-pedido="${id}">
                        <label for="estado-pedido-${id}">Acción del pedido #${id}</label>
                        <select id="estado-pedido-${id}" name="estado">${opciones.map(estado =>
                            `<option value="${estado}">${nombresEstadoPedido[estado]}</option>`).join('')}</select>
                        <div class="referencia-pedido" ${pedido.estado === 'PENDIENTE_PAGO' ? '' : 'hidden'}>
                            <label for="referencia-pedido-${id}">Referencia del pago verificado</label>
                            <input id="referencia-pedido-${id}" name="referenciaPago" maxlength="100"
                                placeholder="Número de operación o comprobante" ${pedido.estado === 'PENDIENTE_PAGO' ? 'required' : 'disabled'}>
                            <small>Comprueba el abono antes de confirmar el pago.</small>
                        </div>
                        <button type="submit" class="btn-accion">Guardar estado</button>
                        <p class="pedido-error" role="status" aria-live="polite"></p>
                    </form>` : 'Sin acciones disponibles'}</td></tr>`;
        }).join('') || '<tr><td colspan="6">No hay pedidos registrados.</td></tr>';
    } catch (error) {
        tbody.innerHTML = `<tr><td colspan="6">${escaparHTML(error.message)}</td></tr>`;
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const tabla = document.getElementById('tabla-ventas');
    tabla?.addEventListener('change', event => {
        if (event.target.name !== 'estado') return;
        const form = event.target.closest('form');
        const referencia = form.elements.referenciaPago;
        const esPago = event.target.value === 'PAGADO';
        form.querySelector('.referencia-pedido').hidden = !esPago;
        referencia.required = esPago;
        referencia.disabled = !esPago;
    });
    tabla?.addEventListener('submit', async event => {
        const form = event.target.closest('.pedido-form');
        if (!form) return;
        event.preventDefault();
        const boton = form.querySelector('button');
        if (boton.disabled) return;
        const estado = form.elements.estado.value;
        const referencia = form.elements.referenciaPago.value.trim();
        const mensaje = form.querySelector('.pedido-error');
        mensaje.textContent = '';
        if (estado === 'PAGADO' && !referencia) {
            mensaje.textContent = 'Escribe la referencia del pago verificado.';
            form.elements.referenciaPago.focus();
            return;
        }
        if (estado === 'CANCELADO' && !confirm('¿Cancelar este pedido pendiente y devolver su stock?')) return;
        boton.disabled = true;
        form.elements.estado.disabled = true;
        try {
            const respuesta = await fetch(`${TIENDA_API}/pedidos/${form.dataset.pedido}/estado`, {
                method: 'PUT',
                headers: {'Content-Type': 'application/json', Authorization: `Bearer ${localStorage.getItem('token_monarca') || ''}`},
                body: JSON.stringify({estado, referenciaPago: estado === 'PAGADO' ? referencia : null})
            });
            const datos = await respuesta.json().catch(() => ({}));
            if (!respuesta.ok) throw new Error(datos.error ||
                (respuesta.status === 403 ? 'No tienes permisos o tu sesión ha vencido.' : 'No se pudo actualizar el pedido.'));
            document.getElementById('mensaje-pedidos').textContent = `Pedido #${form.dataset.pedido} actualizado a ${datos.estado}.`;
            await cargarVentas();
            // Reflejar en inventario el stock devuelto al cancelar.
            if (typeof cargarProductos === 'function') await cargarProductos();
        } catch (error) {
            mensaje.textContent = error.message;
        } finally {
            boton.disabled = false;
            form.elements.estado.disabled = false;
        }
    });
});
