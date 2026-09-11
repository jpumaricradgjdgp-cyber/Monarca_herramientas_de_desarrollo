// sesion.js - Control visual con desencriptación de JWT
document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem('token_monarca');
    const linkPerfil = document.querySelector('.iconos-usuario a[href="login.html"]') || document.getElementById('link-perfil');

    if (token && linkPerfil) {
        try {
            // Desencriptamos el JWT para sacar el correo
            const payload = JSON.parse(atob(token.split('.')[1]));
            const emailReal = payload.sub; 

            const nombreCorto = emailReal.split('@')[0];
            const nombreFormateado = nombreCorto.charAt(0).toUpperCase() + nombreCorto.slice(1);

            let contenedorTexto = document.getElementById('user-name-display');
            if (!contenedorTexto) {
                contenedorTexto = document.createElement('span');
                contenedorTexto.id = 'user-name-display';
                contenedorTexto.style.fontWeight = 'bold';
                contenedorTexto.style.marginRight = '12px';
                contenedorTexto.style.fontSize = '0.95rem';
                contenedorTexto.style.color = '#000';
                contenedorTexto.style.display = 'inline-block';
                contenedorTexto.style.verticalAlign = 'middle';
                
                linkPerfil.parentNode.insertBefore(contenedorTexto, linkPerfil);
            }
            contenedorTexto.innerText = `Hola, ${nombreFormateado}`;

            if (!document.getElementById('btn-logout')) {
                const btnLogout = document.createElement('a');
                btnLogout.href = "#";
                btnLogout.id = "btn-logout";
                btnLogout.style.marginLeft = "12px";
                btnLogout.style.display = "inline-block";
                btnLogout.style.verticalAlign = "middle";
                btnLogout.innerHTML = `<i class="fas fa-sign-out-alt" style="font-size: 1.2rem; color: #d9534f;" title="Cerrar sesión"></i>`;
                
                btnLogout.onclick = (e) => {
                    e.preventDefault();
                    localStorage.clear();
                    alert("Sesión cerrada.");
                    window.location.href = "../index.html"; 
                };
                linkPerfil.parentNode.appendChild(btnLogout);
            }
        } catch(error) {
            console.error("Error leyendo token:", error);
        }
    }
});