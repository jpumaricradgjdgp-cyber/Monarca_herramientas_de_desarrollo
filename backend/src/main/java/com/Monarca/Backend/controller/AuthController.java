package com.Monarca.Backend.controller;

import com.Monarca.Backend.dto.LoginDto;
import com.Monarca.Backend.dto.RegistroDto;
import com.Monarca.Backend.model.Rol;
import com.Monarca.Backend.model.Usuario;
import com.Monarca.Backend.repository.RolRepository;
import com.Monarca.Backend.repository.UsuarioRepository;
import com.Monarca.Backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime; // <-- SOLUCIÓN 1: Importamos LocalDateTime
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permite la conexión con tu HTML/JS
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private JwtUtil jwtUtil; // <-- SOLUCIÓN 2: Inyectamos el JwtUtil

    @PostMapping("/registro")
    public ResponseEntity<?> registrarCliente(@RequestBody RegistroDto dto) {
        // 1. Verificar si el correo ya existe
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("El correo ya está registrado");
        }

        // 2. Crear el nuevo usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(dto.getNombre());
        nuevoUsuario.setApellido(dto.getApellido());
        nuevoUsuario.setEmail(dto.getEmail());
        
        // 3. Encriptar la contraseña antes de guardarla
        nuevoUsuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        
        // Cambiamos LocalDate por LocalDateTime para que coincida con tu Entidad
        nuevoUsuario.setFechaRegistro(LocalDateTime.now());

        // 4. Asignar el Rol de Cliente (Ejemplo: id_rol = 2)
        Rol rolCliente = rolRepository.findById(2)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        nuevoUsuario.setRol(rolCliente);

        // 5. Guardar en MySQL
        usuarioRepository.save(nuevoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED).body("Cuenta creada con éxito");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );
// ... dentro de tu método loginUsuario ...
UserDetails userDetails = (UserDetails) authentication.getPrincipal();
String jwt = jwtUtil.generateToken(userDetails);

// Extraemos el rol principal del usuario (ej: ROLE_ADMIN o ROLE_USER)
String rol = userDetails.getAuthorities().iterator().next().getAuthority();

Map<String, String> response = new HashMap<>();
response.put("token", jwt);
response.put("email", userDetails.getUsername());
response.put("rol", rol); // <-- ¡ESTA ES LA CLAVE!

return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("🚨 ERROR REAL DE LOGIN: " + e.getMessage());
            e.printStackTrace(); 
            
            return new ResponseEntity<>("Credenciales inválidas", HttpStatus.UNAUTHORIZED);
        }
    }
}