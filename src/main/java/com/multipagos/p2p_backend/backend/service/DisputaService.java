package com.multipagos.p2p_backend.backend.service; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.Disputa;
import com.multipagos.p2p_backend.backend.model.Transaccion;
import com.multipagos.p2p_backend.backend.model.Usuario;
import com.multipagos.p2p_backend.backend.repository.DisputaRepository;
import com.multipagos.p2p_backend.backend.repository.TransaccionRepository;
import com.multipagos.p2p_backend.backend.repository.UsuarioRepository;
import com.multipagos.p2p_backend.backend.dto.CrearDisputaRequest; // Importa el DTO
import com.multipagos.p2p_backend.backend.dto.ActualizarDisputaRequest; // Importa el DTO
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DisputaService {

    private final DisputaRepository disputaRepository;
    private final TransaccionRepository transaccionRepository;
    private final UsuarioRepository usuarioRepository;

    public DisputaService(DisputaRepository disputaRepository,
                          TransaccionRepository transaccionRepository,
                          UsuarioRepository usuarioRepository) {
        this.disputaRepository = disputaRepository;
        this.transaccionRepository = transaccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Disputa crearDisputa(Long usuarioReportaId, CrearDisputaRequest request) {
        // 1. Verificar que la transacción exista
        Transaccion transaccion = transaccionRepository.findById(request.getTransaccionId())
            .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada con ID: " + request.getTransaccionId()));

        // 2. Verificar que el usuario que reporta exista
        Usuario usuarioReporta = usuarioRepository.findById(usuarioReportaId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario que reporta no encontrado con ID: " + usuarioReportaId));

        // 3. Opcional: Validar que el usuario que reporta sea parte de la transacción (vendedor o cajero)
        if (!transaccion.getVendedor().getId().equals(usuarioReporta.getId()) &&
            (transaccion.getCajero() == null || !transaccion.getCajero().getId().equals(usuarioReporta.getId()))) {
            throw new IllegalArgumentException("Solo el vendedor o el cajero de la transacción pueden iniciar una disputa.");
        }

        // 4. Verificar que la transacción no tenga ya una disputa abierta
        Optional<Disputa> existingDisputa = disputaRepository.findByTransaccionId(transaccion.getId());
        if (existingDisputa.isPresent() && existingDisputa.get().getEstado().equalsIgnoreCase("abierta")) {
            throw new IllegalArgumentException("Ya existe una disputa abierta para esta transacción.");
        }

        // 5. Crear la entidad Disputa
        Disputa disputa = new Disputa();
        disputa.setTransaccion(transaccion);
        disputa.setUsuarioReporta(usuarioReporta);
        disputa.setMotivoDisputa(request.getMotivoDisputa());
        disputa.setUrlEvidenciaAdicional(request.getUrlEvidenciaAdicional());
        disputa.setEstado("abierta"); // Estado inicial de la disputa

        // Opcional: Actualizar el estado de la transacción a "disputa"
        transaccion.setEstado("disputa");
        transaccionRepository.save(transaccion); // Guardar el cambio de estado en la transacción

        // 6. Guardar la disputa
        return disputaRepository.save(disputa);
    }

    @Transactional
    public Disputa actualizarDisputa(Long disputaId, ActualizarDisputaRequest request) {
        // 1. Verificar que la disputa exista
        Disputa disputa = disputaRepository.findById(disputaId)
            .orElseThrow(() -> new IllegalArgumentException("Disputa no encontrada con ID: " + disputaId));

        // 2. Verificar que el administrador exista y tenga el rol de 'administrador'
        Usuario administrador = usuarioRepository.findById(request.getAdministradorId())
            .orElseThrow(() -> new IllegalArgumentException("Administrador no encontrado con ID: " + request.getAdministradorId()));

        if (!administrador.getRol().equalsIgnoreCase("administrador")) {
            throw new IllegalArgumentException("El usuario con ID " + request.getAdministradorId() + " no es un administrador.");
        }

        // 3. ¡VALIDACIÓN DE REMITENTE DESHABILITADA TEMPORALMENTE PARA DESARROLLO!
    // En producción, esta validación DEBE estar activa y el usuario que reporta debe ser parte de la transacción.
    // if (!transaccion.getVendedor().getId().equals(usuarioReporta.getId()) &&
    //     (transaccion.getCajero() == null || !transaccion.getCajero().getId().equals(usuarioReporta.getId()))) {
    //     throw new IllegalArgumentException("Solo el vendedor o el cajero de la transacción pueden iniciar una disputa.");
    // }

        // 4. Actualizar campos de la disputa
        disputa.setEstado(request.getEstado());
        disputa.setDecisionAdministrador(request.getDecisionAdministrador());
        disputa.setAdministradorAsignado(administrador);
        disputa.setFechaResolucion(LocalDateTime.now());

        // 5. Opcional: Actualizar el estado de la transacción según la resolución de la disputa
        Transaccion transaccion = disputa.getTransaccion();
        if (request.getEstado().equalsIgnoreCase("resuelta_vendedor") || request.getEstado().equalsIgnoreCase("resuelta_cajero")) {
            transaccion.setEstado("resuelta"); // O "completada" si la resolución implica finalizarla
        } else if (request.getEstado().equalsIgnoreCase("cancelada")) {
            transaccion.setEstado("cancelada");
        }
        transaccionRepository.save(transaccion);

        // 6. Guardar la disputa actualizada
        return disputaRepository.save(disputa);
    }

    @Transactional(readOnly = true)
    public List<Disputa> obtenerTodasLasDisputas() {
        return disputaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Disputa> obtenerMisDisputasReportadas(Long usuarioId) {
        return disputaRepository.findByUsuarioReportaId(usuarioId);
    }

    @Transactional(readOnly = true)
    public Optional<Disputa> obtenerDisputaPorId(Long disputaId) {
        return disputaRepository.findById(disputaId);
    }
}
