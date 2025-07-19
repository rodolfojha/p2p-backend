package com.multipagos.p2p_backend.backend.controller; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.Usuario;
import com.multipagos.p2p_backend.backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // Re-habilitar importación
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UsuarioService usuarioService;

    public UserController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Endpoint para que un CAJERO cambie su disponibilidad
    @PostMapping("/set-availability")
    @PreAuthorize("hasAuthority('ROLE_CAJERO')") // Habilitar seguridad
    public ResponseEntity<?> setCajeroAvailability(
        @RequestParam boolean disponible,
        Authentication authentication // ¡Ahora esperamos la autenticación!
    ) {
        try {
            String emailUsuarioAutenticado = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(emailUsuarioAutenticado)
                                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en DB."));
            Long userId = usuario.getId(); // ID dinámico

            Usuario usuarioActualizado = usuarioService.actualizarDisponibilidadCajero(userId, disponible);
            
            // Limpiar datos sensibles antes de devolver
            usuarioActualizado.setPasswordHash(null);
            usuarioActualizado.setAutenticador2faSecreto(null);

            return ResponseEntity.ok(usuarioActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar disponibilidad del cajero: " + e.getMessage());
        }
    }

    // Endpoint para que cualquier usuario autenticado vea su propio perfil
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()") // Habilitar seguridad
    public ResponseEntity<?> getMyProfile(
        Authentication authentication // ¡Ahora esperamos la autenticación!
    ) {
        try {
            String emailUsuarioAutenticado = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(emailUsuarioAutenticado)
                                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en DB."));
            
            // Limpiar datos sensibles antes de devolver
            usuario.setPasswordHash(null);
            usuario.setAutenticador2faSecreto(null);

            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener perfil del usuario: " + e.getMessage());
        }
    }
}
