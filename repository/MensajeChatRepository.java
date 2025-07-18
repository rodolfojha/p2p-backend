package com.multipagos.p2p_backend.backend.repository; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.MensajeChat; // Importa tu clase de modelo
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MensajeChatRepository extends JpaRepository<MensajeChat, Long> {
    // Buscar todos los mensajes de una transacción específica, ordenados por fecha de envío
    List<MensajeChat> findByTransaccionIdOrderByFechaEnvioAsc(Long transaccionId);
}