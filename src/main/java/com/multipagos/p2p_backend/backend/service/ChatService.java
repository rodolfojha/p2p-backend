package com.multipagos.p2p_backend.backend.service; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.MensajeChat;
import com.multipagos.p2p_backend.backend.model.Transaccion;
import com.multipagos.p2p_backend.backend.model.Usuario;
import com.multipagos.p2p_backend.backend.repository.MensajeChatRepository;
import com.multipagos.p2p_backend.backend.repository.TransaccionRepository;
import com.multipagos.p2p_backend.backend.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ChatService {

    private final MensajeChatRepository mensajeChatRepository;
    private final TransaccionRepository transaccionRepository;
    private final UsuarioRepository usuarioRepository;

    public ChatService(MensajeChatRepository mensajeChatRepository,
                       TransaccionRepository transaccionRepository,
                       UsuarioRepository usuarioRepository) {
        this.mensajeChatRepository = mensajeChatRepository;
        this.transaccionRepository = transaccionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public MensajeChat guardarMensaje(Long transaccionId, Long remitenteId, String contenido) {
        // 1. Verificar que la transacción exista
        Transaccion transaccion = transaccionRepository.findById(transaccionId)
            .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada con ID: " + transaccionId));

        // 2. Verificar que el remitente exista
        Usuario remitente = usuarioRepository.findById(remitenteId)
            .orElseThrow(() -> new IllegalArgumentException("Remitente no encontrado con ID: " + remitenteId));

        // 3. ¡RE-HABILITAR VALIDACIÓN! Verificar que el remitente sea parte de la transacción (vendedor o cajero)
        if (!transaccion.getVendedor().getId().equals(remitente.getId()) &&
            (transaccion.getCajero() == null || !transaccion.getCajero().getId().equals(remitente.getId()))) {
            throw new IllegalArgumentException("El remitente no es parte de esta transacción.");
        }

        // 4. Crear y guardar el mensaje
        MensajeChat mensaje = new MensajeChat();
        mensaje.setTransaccion(transaccion);
        mensaje.setRemitente(remitente);
        mensaje.setContenido(contenido);

        return mensajeChatRepository.save(mensaje);
    }

    @Transactional(readOnly = true)
    public List<MensajeChat> obtenerMensajesPorTransaccion(Long transaccionId) {
        return mensajeChatRepository.findByTransaccionIdOrderByFechaEnvioAsc(transaccionId);
    }
}
