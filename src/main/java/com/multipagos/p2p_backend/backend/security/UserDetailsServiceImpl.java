package com.multipagos.p2p_backend.backend.security; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.Usuario; // Importa tu clase Usuario
import com.multipagos.p2p_backend.backend.repository.UsuarioRepository; // Importa tu UsuarioRepository
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // Para roles/autoridades
import java.util.Collections; // Para Collections.singletonList

@Service // Para que Spring lo gestione como un bean
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Convertir el rol del usuario a una autoridad de Spring Security
        // Ej: "vendedor" -> new SimpleGrantedAuthority("ROLE_VENDEDOR")
        // Los roles en Spring Security suelen ir precedidos de "ROLE_"
        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPasswordHash(), // Ya está hasheada
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().toUpperCase()))
        );
    }
}