package com.multipagos.p2p_backend.backend.controller; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.Disputa;
import com.multipagos.p2p_backend.backend.model.Usuario;
import com.multipagos.p2p_backend.backend.service.DisputaService;
import com.multipagos.p2p_backend.backend.service.UsuarioService;
import com.multipagos.p2p_backend.backend.dto.CrearDisputaRequest;
import com.multipagos.p2p_backend.backend.dto.ActualizarDisputaRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Re-habilitar importación
import org.springframework.security.core.Authentication; // Re-habilitar importación
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/disputas")
public class DisputaController {

    private final DisputaService disputaService;
    private final UsuarioService usuarioService;

    public DisputaController(DisputaService disputaService, UsuarioService usuarioService) {
        this.disputaService = disputaService;
        this.usuarioService = usuarioService;
    }

    // Endpoint para que un usuario (vendedor o cajero) inicie una disputa
    @PostMapping("/iniciar")
    @PreAuthorize("isAuthenticated()") // Habilitar seguridad
    public ResponseEntity<?> iniciarDisputa(
        @Valid @RequestBody CrearDisputaRequest request,
        Authentication authentication // ¡Ahora esperamos la autenticación!
    ) {
        try {
            String emailUsuarioAutenticado = authentication.getName();
            Usuario usuarioReporta = usuarioService.findByEmail(emailUsuarioAutenticado)
                                    .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en DB."));
            Long usuarioReportaId = usuarioReporta.getId(); // ID dinámico

            Disputa nuevaDisputa = disputaService.crearDisputa(usuarioReportaId, request);
            return new ResponseEntity<>(nuevaDisputa, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al iniciar la disputa: " + e.getMessage());
        }
    }

    // Endpoint para que un ADMINISTRADOR actualice/resuelva una disputa
    @PutMapping("/resolver/{disputaId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')") // Habilitar seguridad
    public ResponseEntity<?> resolverDisputa(
        @PathVariable Long disputaId,
        @Valid @RequestBody ActualizarDisputaRequest request,
        Authentication authentication // ¡Ahora esperamos la autenticación!
    ) {
        try {
            String emailAdministradorAutenticado = authentication.getName();
            Usuario administrador = usuarioService.findByEmail(emailAdministradorAutenticado)
                                    .orElseThrow(() -> new IllegalStateException("Administrador autenticado no encontrado en DB."));
            request.setAdministradorId(administrador.getId()); // Asignar ID dinámico del admin autenticado

            Disputa disputaActualizada = disputaService.actualizarDisputa(disputaId, request);
            return ResponseEntity.ok(disputaActualizada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al resolver la disputa: " + e.getMessage());
        }
    }

    // Endpoint para que un ADMINISTRADOR vea todas las disputas
    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMINISTRADOR')") // Habilitar seguridad
    public ResponseEntity<?> obtenerTodasLasDisputas() {
        try {
            List<Disputa> disputas = disputaService.obtenerTodasLasDisputas();
            return ResponseEntity.ok(disputas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener todas las disputas: " + e.getMessage());
        }
    }

    // Endpoint para que un usuario vea sus disputas reportadas
    @GetMapping("/mis-disputas")
    @PreAuthorize("isAuthenticated()") // Habilitar seguridad
    public ResponseEntity<?> obtenerMisDisputasReportadas(
        Authentication authentication // ¡Ahora esperamos la autenticación!
    ) {
        try {
            String emailUsuarioAutenticado = authentication.getName();
            Usuario usuario = usuarioService.findByEmail(emailUsuarioAutenticado)
                                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en DB."));
            Long usuarioId = usuario.getId(); // ID dinámico

            List<Disputa> misDisputas = disputaService.obtenerMisDisputasReportadas(usuarioId);
            return ResponseEntity.ok(misDisputas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener mis disputas: " + e.getMessage());
        }
    }

    // Endpoint para obtener una disputa por ID (para cualquier rol si tiene permiso, o para admin)
    @GetMapping("/{disputaId}")
    @PreAuthorize("isAuthenticated()") // Habilitar seguridad
    public ResponseEntity<?> obtenerDisputaPorId(@PathVariable Long disputaId) {
        try {
            Disputa disputa = disputaService.obtenerDisputaPorId(disputaId)
                .orElseThrow(() -> new IllegalArgumentException("Disputa no encontrada con ID: " + disputaId));
            return ResponseEntity.ok(disputa);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener disputa: " + e.getMessage());
        }
    }
}
