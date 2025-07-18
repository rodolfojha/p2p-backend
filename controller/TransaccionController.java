package com.multipagos.p2p_backend.backend.controller; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.Transaccion;
import com.multipagos.p2p_backend.backend.model.Usuario;
import com.multipagos.p2p_backend.backend.service.TransaccionService;
import com.multipagos.p2p_backend.backend.service.UsuarioService;
import com.multipagos.p2p_backend.backend.dto.SolicitudTransaccionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication; // Re-habilitar importación
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final UsuarioService usuarioService;

    public TransaccionController(TransaccionService transaccionService, UsuarioService usuarioService) {
        this.transaccionService = transaccionService;
        this.usuarioService = usuarioService;
    }

    // Endpoint para que un VENDEDOR cree una nueva solicitud de transacción
    @PostMapping("/solicitar")
    @PreAuthorize("hasRole('VENDEDOR')") // Habilitar seguridad
    public ResponseEntity<?> crearSolicitud(
        @Valid @RequestBody SolicitudTransaccionRequest request,
        Authentication authentication // ¡Ahora esperamos la autenticación!
    ) {
        try {
            String emailUsuarioAutenticado = authentication.getName();
            Usuario vendedor = usuarioService.findByEmail(emailUsuarioAutenticado)
                                .orElseThrow(() -> new IllegalStateException("Vendedor autenticado no encontrado en DB."));
            Long vendedorId = vendedor.getId(); // ID dinámico
            
            Transaccion nuevaTransaccion = transaccionService.crearSolicitudTransaccion(vendedorId, request);
            return new ResponseEntity<>(nuevaTransaccion, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear la solicitud de transacción: " + e.getMessage());
        }
    }

    // Endpoint para que un CAJERO vea todas las solicitudes PENDIENTES
    @GetMapping("/pendientes-cajero")
    @PreAuthorize("hasRole('CAJERO')") // Habilitar seguridad
    public ResponseEntity<?> obtenerSolicitudesPendientesCajero() {
        try {
            List<Transaccion> solicitudes = transaccionService.obtenerSolicitudesPendientesParaCajero();
            return ResponseEntity.ok(solicitudes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener solicitudes pendientes: " + e.getMessage());
        }
    }

    // Endpoint para que un CAJERO acepte una solicitud
    @PostMapping("/aceptar/{transaccionId}")
    @PreAuthorize("hasRole('CAJERO')") // Habilitar seguridad
    public ResponseEntity<?> aceptarSolicitud(
        @PathVariable Long transaccionId,
        Authentication authentication // ¡Ahora esperamos la autenticación!
    ) {
        try {
            String emailCajeroAutenticado = authentication.getName();
            Usuario cajero = usuarioService.findByEmail(emailCajeroAutenticado)
                                .orElseThrow(() -> new IllegalStateException("Cajero autenticado no encontrado en DB."));
            Long cajeroId = cajero.getId(); // ID dinámico

            Transaccion transaccionAceptada = transaccionService.aceptarSolicitud(transaccionId, cajeroId);
            return ResponseEntity.ok(transaccionAceptada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al aceptar la solicitud: " + e.getMessage());
        }
    }

    // --- NUEVOS ENDPOINTS PARA ACTUALIZAR EL ESTADO DE LA TRANSACCIÓN ---

    // Endpoint para marcar el pago como iniciado (lo hace el vendedor para depósito, o cajero para retiro)
    @PostMapping("/marcar-pago-iniciado/{transaccionId}")
    @PreAuthorize("isAuthenticated()") // Habilitar seguridad (cualquiera autenticado)
    public ResponseEntity<?> marcarPagoIniciado(
        @PathVariable Long transaccionId,
        @RequestBody Map<String, String> requestBody,
        Authentication authentication // ¡Ahora esperamos la autenticación!
    ) {
        try {
            String urlComprobante = requestBody.get("urlComprobante");
            if (urlComprobante == null || urlComprobante.isEmpty()) {
                return ResponseEntity.badRequest().body("La URL del comprobante es obligatoria.");
            }

            String emailPagador = authentication.getName();
            Usuario pagador = usuarioService.findByEmail(emailPagador)
                                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en DB."));
            Long pagadorId = pagador.getId(); // ID dinámico

            Transaccion updatedTransaccion = transaccionService.marcarPagoIniciado(transaccionId, pagadorId, urlComprobante);
            return ResponseEntity.ok(updatedTransaccion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al marcar pago iniciado: " + e.getMessage());
        }
    }

    // Endpoint para marcar la transacción como completada (lo hace el cajero para depósito, o vendedor para retiro)
    @PostMapping("/marcar-completada/{transaccionId}")
    @PreAuthorize("isAuthenticated()") // Habilitar seguridad (cualquiera autenticado)
    public ResponseEntity<?> marcarTransaccionCompletada(
        @PathVariable Long transaccionId,
        Authentication authentication // ¡Ahora esperamos la autenticación!
    ) {
        try {
            String emailConfirmador = authentication.getName();
            Usuario confirmador = usuarioService.findByEmail(emailConfirmador)
                                    .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado en DB."));
            Long confirmadorId = confirmador.getId(); // ID dinámico

            Transaccion completedTransaccion = transaccionService.marcarTransaccionCompletada(transaccionId, confirmadorId);
            return ResponseEntity.ok(completedTransaccion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al marcar transacción como completada: " + e.getMessage());
        }
    }

    // Endpoint para que un VENDEDOR vea sus propias solicitudes
    @GetMapping("/mis-solicitudes")
    @PreAuthorize("hasRole('VENDEDOR')") // Habilitar seguridad
    public ResponseEntity<?> obtenerMisSolicitudes(
        Authentication authentication // ¡Ahora esperamos la autenticación!
    ) {
        try {
            String emailVendedorAutenticado = authentication.getName();
            Usuario vendedor = usuarioService.findByEmail(emailVendedorAutenticado)
                                .orElseThrow(() -> new IllegalStateException("Vendedor autenticado no encontrado en DB."));
            Long vendedorId = vendedor.getId(); // ID dinámico

            List<Transaccion> misSolicitudes = transaccionService.obtenerMisSolicitudes(vendedorId);
            return ResponseEntity.ok(misSolicitudes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener mis solicitudes: " + e.getMessage());
        }
    }

    // Endpoint para que un ADMINISTRADOR vea todas las transacciones
    @GetMapping("/todas")
    @PreAuthorize("hasRole('ADMINISTRADOR')") // Habilitar seguridad
    public ResponseEntity<?> obtenerTodasLasTransacciones() {
        try {
            List<Transaccion> todasLasTransacciones = transaccionService.obtenerTodasLasTransacciones();
            return ResponseEntity.ok(todasLasTransacciones);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al obtener todas las transacciones: " + e.getMessage());
        }
    }
}
