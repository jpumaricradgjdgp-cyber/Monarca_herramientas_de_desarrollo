document.addEventListener('DOMContentLoaded', () => {
    const formLogin = document.getElementById('form-login');

    if (formLogin) {
        formLogin.addEventListener('submit', async function(event) {
            event.preventDefault();

            // Capturamos el valor del input usuario (que en tu backend debe ser el email)
            const email = document.getElementById('usuario').value;
            const password = document.getElementById('password').value;

            await iniciarSesion(email, password);
        });
    }
});

async function iniciarSesion(email, password) {
    try {
        const datosLogin = { email: email, password: password };

        const respuesta = await fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(datosLogin)
        });

        if (respuesta.ok) {
            const data = await respuesta.json();
            
            // 🚨 LA PRUEBA DE FUEGO: Esto detendrá la pantalla y te mostrará el valor real
            alert("ATENCIÓN: El rol que envió Java es: " + data.rol);
            
            localStorage.setItem('token_monarca', data.token);
            localStorage.setItem('rol_monarca', data.rol); 

            // Convertimos a mayúsculas para evitar errores y usamos includes()
            const rolUsuario = data.rol ? data.rol.toUpperCase() : "VACIO";

            if (rolUsuario.includes('ADMIN')) {
                window.location.href = "admin.html";
            } else {
                window.location.href = "../index.html"; 
            }
        } else {
            alert("Correo o contraseña incorrectos.");
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Error al conectar con el servidor.');
    }
}

// Lógica para mostrar/ocultar la contraseña
const togglePassword = document.getElementById('toggle-password');
const passwordInput = document.getElementById('password');

if (togglePassword) {
    togglePassword.addEventListener('click', () => {
        // Cambiar tipo de input
        const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
        passwordInput.setAttribute('type', type);
        // Alternar icono
        togglePassword.classList.toggle('fa-eye-slash');
    });
}