package com.multipagos.p2p_backend.backend.service; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.Transaccion;
import com.multipagos.p2p_backend.backend.model.Usuario;
import com.multipagos.p2p_backend.backend.model.MetodoPagoUsuario;
import com.multipagos.p2p_backend.backend.repository.TransaccionRepository;
import com.multipagos.p2p_backend.backend.repository.UsuarioRepository;
import com.multipagos.p2p_backend.backend.repository.MetodoPagoUsuarioRepository;
import com.multipagos.p2p_backend.backend.dto.SolicitudTransaccionRequest; // Importa el DTO
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode; // Para el redondeo de BigDecimal
import java.time.LocalDateTime;
import java.util.List; // Importa List

import org.springframework.messaging.simp.SimpMessagingTemplate; // Importación para WebSockets

// Importar ComisionService
import com.multipagos.p2p_backend.backend.service.ComisionService; // <-- NUEVA IMPORTACIÓN

@Service
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final UsuarioRepository usuarioRepository;
    private final MetodoPagoUsuarioRepository metodoPagoUsuarioRepository;
    private final MetodoPagoUsuarioService metodoPagoUsuarioService; // Para reutilizar el servicio de métodos de pago
    private final SimpMessagingTemplate messagingTemplate; // Declara SimpMessagingTemplate para WebSockets
    private final ComisionService comisionService; // <-- Declara ComisionService

    // Constructor para inyección de dependencias
    public TransaccionService(TransaccionRepository transaccionRepository,
                              UsuarioRepository usuarioRepository,
                              MetodoPagoUsuarioRepository metodoPagoUsuarioRepository,
                              MetodoPagoUsuarioService metodoPagoUsuarioService,
                              SimpMessagingTemplate messagingTemplate,
                              ComisionService comisionService) { // <-- Añade ComisionService
        this.transaccionRepository = transaccionRepository;
        this.usuarioRepository = usuarioRepository;
        this.metodoPagoUsuarioRepository = metodoPagoUsuarioRepository;
        this.metodoPagoUsuarioService = metodoPagoUsuarioService;
        this.messagingTemplate = messagingTemplate;
        this.comisionService = comisionService; // <-- Asigna
    }

    @Transactional
    public Transaccion solicitarTransaccion(SolicitudTransaccionRequest request, String emailVendedor) { // Cambiado a recibir email
        // 1. Verificar que el vendedor exista
        Usuario vendedor = usuarioRepository.findByEmail(emailVendedor)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado con email: " + emailVendedor));

        // 2. Verificar que el método de pago del vendedor exista y le pertenezca
        MetodoPagoUsuario metodoPagoVendedor = metodoPagoUsuarioRepository.findByIdAndUsuarioId(
                request.getMetodoPagoVendedorId(), vendedor.getId()) // Usar vendedor.getId()
                .orElseThrow(() -> new IllegalArgumentException("Método de pago del vendedor no encontrado o no le pertenece."));

        // 3. Validar el tipo de operación
        if (!request.getTipoOperacion().equalsIgnoreCase("deposito") &&
            !request.getTipoOperacion().equalsIgnoreCase("retiro")) {
            throw new IllegalArgumentException("Tipo de operación inválido. Debe ser 'deposito' o 'retiro'.");
        }

        // 4. Validar la opción de comisión
        if (!request.getOpcionComision().equalsIgnoreCase("restar") &&
            !request.getOpcionComision().equalsIgnoreCase("agregar")) {
            throw new IllegalArgumentException("Opción de comisión inválida. Debe ser 'restar' o 'agregar'.");
        }

        // 5. Calcular comisiones (ejemplo de lógica, puedes ajustar los porcentajes)
        BigDecimal comisionPlataformaPorcentaje = new BigDecimal("0.01"); // 1%
        BigDecimal montoBase = request.getMonto();
        BigDecimal comisionBruta = montoBase.multiply(comisionPlataformaPorcentaje).setScale(2, RoundingMode.HALF_UP);

        BigDecimal montoNetoVendedor;
        if (request.getOpcionComision().equalsIgnoreCase("restar")) {
            montoNetoVendedor = montoBase.subtract(comisionBruta).setScale(2, RoundingMode.HALF_UP);
        } else { // "agregar"
            montoNetoVendedor = montoBase;
        }

        // 6. Crear la entidad Transaccion
        Transaccion transaccion = new Transaccion();
        transaccion.setVendedor(vendedor);
        transaccion.setTipoOperacion(request.getTipoOperacion());
        transaccion.setMonto(montoBase);
        transaccion.setMoneda(request.getMoneda());
        transaccion.setComisionBruta(comisionBruta);
        transaccion.setMontoNetoVendedor(montoNetoVendedor);
        transaccion.setEstado("pendiente"); // Estado inicial
        transaccion.setMetodoPagoVendedor(metodoPagoVendedor);
        transaccion.setFechaSolicitud(LocalDateTime.now()); // Establecer fecha de solicitud

        // 7. Guardar la transacción
        Transaccion savedTransaccion = transaccionRepository.save(transaccion);

        // --- Notificación WebSocket a Cajeros (nueva solicitud) ---
        messagingTemplate.convertAndSend("/topic/solicitudes-pendientes", savedTransaccion);

        return savedTransaccion;
    }

    // --- Métodos para que el cajero vea y acepte solicitudes ---

    @Transactional(readOnly = true)
    public List<Transaccion> getTransaccionesPendientes() { // Renombrado para consistencia
        return transaccionRepository.findByEstado("pendiente");
    }

    @Transactional
    public Transaccion aceptarTransaccion(Long transaccionId, String emailCajero) { // Cambiado a recibir email
        // 1. Verificar que la transacción exista y esté en estado 'pendiente'
        Transaccion transaccion = transaccionRepository.findById(transaccionId)
            .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada con ID: " + transaccionId));

        if (!transaccion.getEstado().equalsIgnoreCase("pendiente")) {
            throw new IllegalArgumentException("La transacción no está en estado pendiente.");
        }

        // 2. Verificar que el cajero exista y tenga el rol de 'cajero'
        Usuario cajero = usuarioRepository.findByEmail(emailCajero) // Buscar por email
            .orElseThrow(() -> new IllegalArgumentException("Cajero no encontrado con email: " + emailCajero));

        if (!cajero.getRol().equalsIgnoreCase("cajero")) {
            throw new IllegalArgumentException("El usuario con email " + emailCajero + " no es un cajero.");
        }
        if (!cajero.getDisponibilidadCajero()) {
            throw new IllegalArgumentException("El cajero no está disponible para aceptar solicitudes.");
        }

        // 3. Asignar cajero y cambiar estado
        transaccion.setCajero(cajero);
        transaccion.setEstado("aceptada");
        transaccion.setFechaAceptacion(LocalDateTime.now());

        // 4. Guardar y devolver la transacción actualizada
        Transaccion acceptedTransaccion = transaccionRepository.save(transaccion);

        // --- Notificación WebSocket al Vendedor (su solicitud fue aceptada) ---
        // Envía un mensaje privado al vendedor de esta transacción
        messagingTemplate.convertAndSendToUser(
            acceptedTransaccion.getVendedor().getEmail(),
            "/queue/transaccion-updates",
            acceptedTransaccion
        );

        // --- Notificación WebSocket a todos los Cajeros (la solicitud ya no está pendiente) ---
        messagingTemplate.convertAndSend("/topic/solicitudes-actualizadas", acceptedTransaccion.getId());

        return acceptedTransaccion;
    }

    // --- NUEVOS MÉTODOS PARA ACTUALIZAR EL ESTADO DE LA TRANSACCIÓN ---

    @Transactional
    public Transaccion marcarPagoIniciado(Long transaccionId, String emailPagador, String urlComprobante) { // Cambiado a recibir email
        Transaccion transaccion = transaccionRepository.findById(transaccionId)
            .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada con ID: " + transaccionId));

        // Validar estado actual
        if (!transaccion.getEstado().equalsIgnoreCase("aceptada")) {
            throw new IllegalArgumentException("La transacción no está en estado 'aceptada' para iniciar el pago.");
        }

        // Obtener el usuario pagador
        Usuario pagador = usuarioRepository.findByEmail(emailPagador)
            .orElseThrow(() -> new IllegalArgumentException("Usuario pagador no encontrado con email: " + emailPagador));

        // Validar que el pagador sea el vendedor (para depósito) o el cajero (para retiro)
        if (transaccion.getTipoOperacion().equalsIgnoreCase("deposito")) {
            if (!transaccion.getVendedor().getId().equals(pagador.getId())) {
                throw new IllegalArgumentException("Solo el vendedor puede marcar el pago iniciado para un depósito.");
            }
        } else { // Retiro
            if (transaccion.getCajero() == null || !transaccion.getCajero().getId().equals(pagador.getId())) {
                throw new IllegalArgumentException("Solo el cajero puede marcar el pago iniciado para un retiro.");
            }
        }

        transaccion.setEstado("en_proceso_pago");
        transaccion.setFechaPagoIniciado(LocalDateTime.now());
        transaccion.setUrlComprobantePago(urlComprobante); // Guarda la URL del comprobante

        Transaccion updatedTransaccion = transaccionRepository.save(transaccion);

        // --- Notificación WebSocket a la otra parte ---
        String destinatarioEmail = transaccion.getTipoOperacion().equalsIgnoreCase("deposito") ?
                                   transaccion.getCajero().getEmail() : transaccion.getVendedor().getEmail();
        
        messagingTemplate.convertAndSendToUser(
            destinatarioEmail,
            "/queue/transaccion-updates",
            updatedTransaccion
        );

        return updatedTransaccion;
    }

    @Transactional
    public Transaccion marcarCompletada(Long transaccionId, String emailConfirmador) { // Cambiado a recibir email
        Transaccion transaccion = transaccionRepository.findById(transaccionId)
            .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada con ID: " + transaccionId));

        // Validar estado actual
        if (!transaccion.getEstado().equalsIgnoreCase("en_proceso_pago")) {
            throw new IllegalArgumentException("La transacción no está en estado 'en_proceso_pago' para ser completada.");
        }

        // Obtener el usuario confirmador
        Usuario confirmador = usuarioRepository.findByEmail(emailConfirmador)
            .orElseThrow(() -> new IllegalArgumentException("Usuario confirmador no encontrado con email: " + emailConfirmador));

        // Validar que el confirmador sea la otra parte (el que recibe el pago)
        if (transaccion.getTipoOperacion().equalsIgnoreCase("deposito")) {
            if (transaccion.getCajero() == null || !transaccion.getCajero().getId().equals(confirmador.getId())) {
                throw new IllegalArgumentException("Solo el cajero puede confirmar la recepción del pago para un depósito.");
            }
        } else { // Retiro
            if (!transaccion.getVendedor().getId().equals(confirmador.getId())) {
                throw new IllegalArgumentException("Solo el vendedor puede confirmar la recepción del pago para un retiro.");
            }
        }

        transaccion.setEstado("completada");
        transaccion.setFechaConfirmacionFinal(LocalDateTime.now());

        Transaccion completedTransaccion = transaccionRepository.save(transaccion);

        // --- Notificación WebSocket a AMBAS partes y a administradores/tópico general ---
        // Notificar al vendedor
        messagingTemplate.convertAndSendToUser(
            completedTransaccion.getVendedor().getEmail(),
            "/queue/transaccion-updates",
            completedTransaccion
        );
        // Notificar al cajero
        if (completedTransaccion.getCajero() != null) {
            messagingTemplate.convertAndSendToUser(
                completedTransaccion.getCajero().getEmail(),
                "/queue/transaccion-updates",
                completedTransaccion
            );
        }
        // Notificar a un tópico de auditoría o administradores si es necesario
        messagingTemplate.convertAndSend("/topic/transacciones-completadas", completedTransaccion.getId());

        // Aquí se llamaría al servicio de comisiones para registrar la comisión
        comisionService.registrarComision(completedTransaccion); // <-- ¡Llamada al servicio de comisiones!

        return completedTransaccion;
    }

    // --- Métodos para que el vendedor vea sus propias solicitudes ---
    @Transactional(readOnly = true)
    public List<Transaccion> getTransaccionesByVendedorEmail(String emailVendedor) { // Renombrado para consistencia
        return transaccionRepository.findByVendedor_Email(emailVendedor); // Corregido el nombre del método
    }

    // NUEVO MÉTODO: Obtener transacciones asignadas al cajero por su email
    @Transactional(readOnly = true)
    public List<Transaccion> getTransaccionesAsignadasACajero(String emailCajero) {
        return transaccionRepository.findByCajero_Email(emailCajero); // Corregido el nombre del método
    }

    // --- Métodos para que el administrador vea todas las transacciones ---
    @Transactional(readOnly = true)
    public List<Transaccion> getAllTransacciones() { // Renombrado para consistencia
        return transaccionRepository.findAll();
    }
}
