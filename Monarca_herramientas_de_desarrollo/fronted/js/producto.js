document.addEventListener("DOMContentLoaded", async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const idProducto = urlParams.get('id');

    let productoActual = null; 
    let tallaSeleccionada = null; // Variable para guardar la talla que el cliente elija

    if (idProducto) {
        try {
            const respuesta = await fetch(`http://localhost:8080/api/productos/${idProducto}`);
            if (!respuesta.ok) throw new Error("Producto no encontrado");
            
            productoActual = await respuesta.json();

            document.getElementById("prod-titulo").innerText = productoActual.nombre;
            document.getElementById("prod-precio").innerText = `S/ ${productoActual.precioBase.toFixed(2)}`;
            document.getElementById("prod-descripcion").innerText = productoActual.descripcion;
            
            const imgPrincipal = document.getElementById("img-principal");
            if(imgPrincipal) imgPrincipal.src = productoActual.imagen || "../img/mariposa.png";
            
            const prodCategoria = document.getElementById("prod-categoria");
            if(prodCategoria && productoActual.familiaProducto) {
                prodCategoria.innerText = productoActual.familiaProducto.nombre;
            }

            // --- LÓGICA PARA DIBUJAR LAS TALLAS ---
            const contenedorTallas = document.getElementById("contenedor-tallas");
            if(contenedorTallas && productoActual.talla) {
                contenedorTallas.innerHTML = ''; 
                const tallasArray = productoActual.talla.split(',');

                tallasArray.forEach(talla => {
                    const btnTalla = document.createElement("button");
                    btnTalla.innerText = talla.trim();
                    
                    // Estilos rápidos para el botón de talla
                    btnTalla.style.padding = "10px 15px";
                    btnTalla.style.border = "1px solid #ccc";
                    btnTalla.style.background = "#fff";
                    btnTalla.style.cursor = "pointer";
                    btnTalla.style.fontWeight = "bold";

                    // Evento al hacer clic en una talla
                    btnTalla.addEventListener("click", () => {
                        // 1. Limpiar color de todos los botones
                        contenedorTallas.querySelectorAll("button").forEach(b => {
                            b.style.background = "#fff";
                            b.style.color = "#000";
                            b.style.borderColor = "#ccc";
                        });
                        // 2. Pintar de negro el seleccionado
                        btnTalla.style.background = "#1a1a1a";
                        btnTalla.style.color = "#fff";
                        btnTalla.style.borderColor = "#1a1a1a";
                        
                        // 3. Guardar la talla en memoria
                        tallaSeleccionada = talla.trim();
                    });

                    contenedorTallas.appendChild(btnTalla);
                });
            }

        } catch (error) {
            console.error("Error cargando el producto:", error);
            document.getElementById("prod-titulo").innerText = "Producto no disponible";
        }
    }

    // --- LÓGICA DEL BOTÓN AGREGAR ---
    const btnAgregar = document.querySelector(".btn-agregar");
    if (btnAgregar) {
        btnAgregar.addEventListener("click", (e) => {
            e.preventDefault();
            
            if (productoActual) {
                // VERIFICACIÓN: ¿Eligió una talla?
                if (productoActual.talla && !tallaSeleccionada) {
                    alert("¡Ups! Por favor selecciona una talla primero.");
                    return; // Detenemos la función aquí
                }

                if (typeof agregarAlCarrito === "function") {
                    // Le incrustamos la talla al producto antes de mandarlo al carrito
                    productoActual.tallaSeleccionada = tallaSeleccionada;
                    agregarAlCarrito(productoActual);
                }
            }
        });
    }
});