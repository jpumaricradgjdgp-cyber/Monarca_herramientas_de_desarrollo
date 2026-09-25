// Contrato compartido por catálogo, detalle, carrito y checkout.
const TIENDA_API = 'http://localhost:8080/api';
const TIENDA_RAIZ = new URL('../', document.currentScript.src);
function paginaTienda(nombre) { return new URL(`Paginas/${nombre}`, TIENDA_RAIZ).href; }
function escaparHTML(valor) {
    return String(valor ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;'}[c]));
}
function imagenHTTPS(valor) {
    try { const url = new URL(valor); return url.protocol === 'https:' ? url.href : ''; }
    catch { return ''; }
}
function imagenProductoHTML(producto) {
    const url = imagenHTTPS(producto.imagen);
    return url ? `<img src="${escaparHTML(url)}" alt="${escaparHTML(producto.nombre)}">` : '<span class="sin-imagen">Imagen no disponible</span>';
}
function variantesDisponibles(producto) {
    return (producto.variantes || []).filter(v => Number.isSafeInteger(v.idVariante) && v.stock > 0);
}
async function obtenerTienda(ruta, autenticado = false) {
    const headers = autenticado ? {Authorization: `Bearer ${localStorage.getItem('token_monarca') || ''}`} : {};
    const respuesta = await fetch(`${TIENDA_API}${ruta}`, {headers});
    if (!respuesta.ok) throw new Error(`No se pudo cargar la tienda (${respuesta.status}).`);
    return respuesta.json();
}
