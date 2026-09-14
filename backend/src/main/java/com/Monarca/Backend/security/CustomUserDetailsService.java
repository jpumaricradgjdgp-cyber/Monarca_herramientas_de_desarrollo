package com.Monarca.Backend.security;

import com.Monarca.Backend.model.Rol;
import com.Monarca.Backend.model.Usuario;
import com.Monarca.Backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscamos al usuario por su email
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));

        // Retornamos un objeto User de Spring Security
        return new User(
                usuario.getEmail(),
                usuario.getPassword(),
                mapRolesToAuthorities(usuario.getRol())
        );
    }

    // Método para convertir tu entidad Rol a GrantedAuthority de Spring Security
  // Método para convertir tu entidad Rol a GrantedAuthority de Spring Security
    private Collection<GrantedAuthority> mapRolesToAuthorities(Rol rol) {
        // Tu BD ya tiene "ROLE_ADMIN", así que úsalo tal cual.
        // Spring Security leerá ese String y lo comparará directamente con tu @PreAuthorize
        return Collections.singletonList(new SimpleGrantedAuthority(rol.getNombre()));
    }
}