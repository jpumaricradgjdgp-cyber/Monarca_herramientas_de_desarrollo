// ==========================================
// LÓGICA DEL CARRITO DE COMPRAS - MONARCA
// ==========================================

// Usamos "monarca_carrito" para que coincida con tu página de Checkout
let carrito = JSON.parse(localStorage.getItem("monarca_carrito")) || [];

// --- 1. MANEJO DEL PANEL VISUAL ---
document.addEventListener("DOMContentLoaded", () => {
    const btnAbrir = document.getElementById("btn-abrir-carrito-header");
    const btnCerrar = document.getElementById("cerrar-carrito");
    const overlay = document.getElementById("carrito-overlay");
    const panel = document.getElementById("panel-carrito");

    // Abrir carrito
    if(btnAbrir) {
        btnAbrir.addEventListener("click", (e) => {
            e.preventDefault();
            panel.classList.add("activo");
            overlay.classList.add("activo");
            renderizarCarrito(); // Dibuja la ropa al abrir
        });
    }

    // Cerrar carrito (botón X o clic afuera)
    if(btnCerrar) btnCerrar.addEventListener("click", cerrarPanel);
    if(overlay) overlay.addEventListener("click", cerrarPanel);

    function cerrarPanel() {
        if(panel) panel.classList.remove("activo");
        if(overlay) overlay.classList.remove("activo");
    }

    actualizarIconoCarrito();
});

// --- 2. FUNCIONES DE DATOS ---
function guardarCarrito() {
    localStorage.setItem("monarca_carrito", JSON.stringify(carrito));
}

function agregarAlCarrito(producto) {
    // 1. Nos aseguramos de que tenga una talla (si no eligió, le ponemos "Única")
    const talla = producto.tallaSeleccionada || "Única";
    
    // 2. Buscamos si ya existe el MISMO producto en la MISMA talla
    const existente = carrito.find(item => item.idProducto === producto.idProducto && item.tallaSeleccionada === talla);
    
    if (existente) {
        existente.cantidad += 1;
    } else {
        // Clonamos el producto y agregamos las traducciones para que el Checkout.html lo entienda
        const nuevoProducto = { 
            ...producto, 
            cantidad: 1, 
            tallaSeleccionada: talla,
            titulo: producto.nombre || producto.sku, // Traducción para el checkout
            precio: producto.precioBase,             // Traducción para el checkout
            talla: talla                               // Traducción para el checkout
        };
        carrito.push(nuevoProducto);
    }
    
    guardarCarrito();
    actualizarIconoCarrito();

    const panel = document.getElementById("panel-carrito");
    if(panel && panel.classList.contains("activo")) {
        renderizarCarrito();
    } else {
        alert(`¡${producto.nombre || producto.sku} (Talla: ${talla}) se agregó a la bolsa!`);
    }
}

// --- 3. RENDERIZADO VISUAL EN EL PANEL ---
// --- 3. RENDERIZADO VISUAL EN EL PANEL ---
function renderizarCarrito() {
    const contenedorItems = document.getElementById("carrito-items");
    if(!contenedorItems) return;

    contenedorItems.innerHTML = "";

    if(carrito.length === 0) {
        contenedorItems.innerHTML = "<p style='text-align:center; margin-top:30px; color:#666;'>Tu bolsa está vacía.</p>";
    } else {
        carrito.forEach((item, index) => {
            const img = item.imagen ? item.imagen : "../img/mariposa.png";
            
            // 🔥 VARIABLES A PRUEBA DE BALAS (Soporta formato viejo y nuevo)
            const tituloSeguro = item.nombre || item.titulo || item.sku || "Producto Monarca";
            const precioSeguro = item.precioBase || item.precio || 0;
            const tallaSegura = item.tallaSeleccionada || item.talla || "Única";

            contenedorItems.innerHTML += `
                <div class="carrito-item">
                    <img src="${img}" alt="${tituloSeguro}">
                    <div class="carrito-item-info">
                        <div class="carrito-item-header">
                            <div>
                                <h4>${tituloSeguro}</h4>
                                <p style="margin: 2px 0 5px 0; font-size: 13px; color: #666;">Talla: <strong>${tallaSegura}</strong></p>
                            </div>
                            <button onclick="eliminarDelCarrito(${index})" style="background:none; border:none; cursor:pointer; color:#df4b4b;">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                        <p class="precio-txt">S/ ${precioSeguro.toFixed(2)}</p>
                        <div class="controles-cantidad">
                            <button onclick="cambiarCantidad(${index}, -1)">-</button>
                            <span>${item.cantidad}</span>
                            <button onclick="cambiarCantidad(${index}, 1)">+</button>
                        </div>
                    </div>
                </div>
            `;
        });
    }

    // 🔥 Actualizar Totales a prueba de balas
    const subtotal = carrito.reduce((suma, item) => suma + ((item.precioBase || item.precio || 0) * item.cantidad), 0);
    const costoEnvio = subtotal > 0 ? 10.00 : 0.00;
    const total = subtotal + costoEnvio;

    const lblSubtotal = document.getElementById("carrito-subtotal");
    const lblEnvio = document.getElementById("carrito-envio");
    const lblTotal = document.getElementById("carrito-total");

    if(lblSubtotal) lblSubtotal.innerText = `S/ ${subtotal.toFixed(2)}`;
    if(lblEnvio) lblEnvio.innerText = `S/ ${costoEnvio.toFixed(2)}`;
    if(lblTotal) lblTotal.innerText = `S/ ${total.toFixed(2)}`;
}

// --- 4. CONTROLES DE BOTONES INTERNOS ---
function cambiarCantidad(index, cambio) {
    carrito[index].cantidad += cambio;
    if(carrito[index].cantidad <= 0) {
        eliminarDelCarrito(index);
    } else {
        guardarCarrito();
        renderizarCarrito();
        actualizarIconoCarrito();
    }
}

function eliminarDelCarrito(index) {
    carrito.splice(index, 1);
    guardarCarrito();
    renderizarCarrito();
    actualizarIconoCarrito();
}

function actualizarIconoCarrito() {
    const contador = document.getElementById("cart-count");
    if (contador) {
        const totalPrendas = carrito.reduce((suma, item) => suma + item.cantidad, 0);
        contador.innerText = totalPrendas;
    }
}

// ==========================================
// 5. PROCESAR COMPRA (ENVÍO AL BACKEND)
// ==========================================
// ==========================================
// 5. REDIRECCIÓN AL CHECKOUT
// ==========================================
document.addEventListener("DOMContentLoaded", () => {
    const btnFinalizar = document.getElementById("btn-finalizar-compra");
    
    if (btnFinalizar) {
        btnFinalizar.addEventListener("click", (e) => {
            // 1. Detenemos cualquier acción automática del HTML
            e.preventDefault(); 

            // 2. Validar que el carrito no esté vacío
            if (carrito.length === 0) {
                alert("Tu carrito está vacío. ¡Agrega prendas hermosas primero!");
                return;
            }

            // 3. Validar que el usuario esté logueado
            const token = localStorage.getItem("token_monarca"); 
            if (!token) {
                alert("Debes iniciar sesión en Monarca para finalizar tu compra.");
                // Asumimos que login.html está en la misma carpeta
                window.location.href = "login.html"; 
                return;
            }

            // 4. Si todo está perfecto, forzamos el viaje a la página de Checkout
            window.location.href = "checkout.html";
        });
    }
});