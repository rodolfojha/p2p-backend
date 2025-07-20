package com.multipagos.p2p_backend.backend.controller;

import com.multipagos.p2p_backend.backend.model.Transaccion;
import com.multipagos.p2p_backend.backend.service.TransaccionService;
import com.multipagos.p2p_backend.backend.dto.SolicitudTransaccionRequest;
import com.multipagos.p2p_backend.backend.dto.MarcarPagoIniciadoRequest; // Importar el nuevo DTO
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;

    public TransaccionController(TransaccionService transaccionService) {
        this.transaccionService = transaccionService;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<Transaccion> solicitarTransaccion(@RequestBody SolicitudTransaccionRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailVendedor = authentication.getName();

        Transaccion nuevaTransaccion = transaccionService.solicitarTransaccion(request, emailVendedor);
        return new ResponseEntity<>(nuevaTransaccion, HttpStatus.CREATED);
    }

    @PostMapping("/aceptar/{transaccionId}")
    public ResponseEntity<Transaccion> aceptarTransaccion(@PathVariable Long transaccionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailCajero = authentication.getName();

        Transaccion transaccionAceptada = transaccionService.aceptarTransaccion(transaccionId, emailCajero);
        return ResponseEntity.ok(transaccionAceptada);
    }

    // Endpoint para que un usuario (vendedor o cajero) marque el pago como iniciado
    @PostMapping("/marcar-pago-iniciado/{transaccionId}")
    public ResponseEntity<Transaccion> marcarPagoIniciado(@PathVariable Long transaccionId, 
                                                        @RequestBody MarcarPagoIniciadoRequest request) { // Cambiado a DTO
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailPagador = authentication.getName();

        // Pasar la URL del comprobante desde el DTO
        Transaccion transaccionActualizada = transaccionService.marcarPagoIniciado(transaccionId, emailPagador, request.getUrlComprobante());
        return ResponseEntity.ok(transaccionActualizada);
    }

    @PostMapping("/marcar-completada/{transaccionId}")
    public ResponseEntity<Transaccion> marcarCompletada(@PathVariable Long transaccionId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailConfirmador = authentication.getName();

        Transaccion transaccionActualizada = transaccionService.marcarCompletada(transaccionId, emailConfirmador);
        return ResponseEntity.ok(transaccionActualizada);
    }

    @GetMapping("/mis-solicitudes")
    public ResponseEntity<List<Transaccion>> getMyTransactions() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailUsuario = authentication.getName();
        List<Transaccion> transacciones = transaccionService.getTransaccionesByVendedorEmail(emailUsuario);
        return ResponseEntity.ok(transacciones);
    }

    @GetMapping("/pendientes-cajero")
    public ResponseEntity<List<Transaccion>> getPendingTransactionsForCajero() {
        List<Transaccion> transacciones = transaccionService.getTransaccionesPendientes();
        return ResponseEntity.ok(transacciones);
    }

    @GetMapping("/cajero/mis-asignadas")
    public ResponseEntity<List<Transaccion>> getAssignedTransactionsForCajero() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String emailCajero = authentication.getName();
        List<Transaccion> transaccionesAsignadas = transaccionService.getTransaccionesAsignadasACajero(emailCajero);
        return ResponseEntity.ok(transaccionesAsignadas);
    }

    @GetMapping("/todas")
    public ResponseEntity<List<Transaccion>> getAllTransactions() {
        List<Transaccion> transacciones = transaccionService.getAllTransacciones();
        return ResponseEntity.ok(transacciones);
    }
}
