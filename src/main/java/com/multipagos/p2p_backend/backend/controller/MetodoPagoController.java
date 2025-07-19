package com.multipagos.p2p_backend.backend.controller; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.MetodoPagoUsuario;
import com.multipagos.p2p_backend.backend.model.Usuario;
import com.multipagos.p2p_backend.backend.service.MetodoPagoUsuarioService;
import com.multipagos.p2p_backend.backend.service.UsuarioService;
import com.multipagos.p2p_backend.backend.dto.MetodoPagoUsuarioRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // Re-habilitar importación
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/metodos-pago")
public class MetodoPagoController {

    private final MetodoPagoUsuarioService metodoPagoUsuarioService;
    private final UsuarioService usuarioService; // Para obtener el ID del usuario autenticado

    public MetodoPagoController(MetodoPagoUsuarioService metodoPagoUsuarioService, UsuarioService usuarioService) {
        this.metodoPagoUsuarioService = metodoPagoUsuarioService;
        this.usuarioService = usuarioService;
    }

    // Endpoint para añadir un nuevo método de pago para el usuario autenticado
    @PostMapping
    @PreAuthorize("isAuthenticated()") // Habilitar seguridad
    public ResponseEntity<?> agregarMetodoPago(
        @Valid @RequestBody MetodoPagoUsuarioRequest request,
        Authentication authentication // ¡Ahora esperamos la autenticación!
    ) {
        try {
            String emailUsuarioAutenticado = authentication.getName();
            Usuario usuario = usuarioService.obtenerUsuarioPorEmail(emailUsuarioAutenticado); // Usar el método del servicio
            Long userId = usuario.getId(); // ID dinámico

            MetodoPagoUsuario nuevoMetodo = metodoPagoUsuarioService.crearMetodoPago(userId, request);
            return new ResponseEntity<>(nuevoMetodo, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al agregar método de pago: " + e.getMessage());
        }
    }

    // Endpoint para obtener todos los métodos de pago del usuario autenticado
    @GetMapping
    @PreAuthorize("isAuthenticated()") // Habilitar seguridad
    public ResponseEntity<?> obtenerMisMetodosPago(
        Authentication authentication // ¡Ahora esperamos la autenticación!
    ) {
        try {
            String emailUsuarioAutenticado = authentication.getName();
            Usuario usuario = usuarioService.obtenerUsuarioPorEmail(emailUsuarioAutenticado); // Usar el método del servicio
            Long userId = usuario.getId(); // ID dinámico

            List<MetodoPagoUsuario> metodos = metodoPagoUsuarioService.obtenerMetodosPagoPorUsuario(userId);
            return ResponseEntity.ok(metodos);
        } catch (IllegalArgumentException e) {
             return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener métodos de pago: " + e.getMessage());
        }
    }
}
