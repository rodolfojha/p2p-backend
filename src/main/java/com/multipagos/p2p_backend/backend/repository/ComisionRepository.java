package com.multipagos.p2p_backend.backend.repository; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.Comision; // Importa tu clase de modelo
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ComisionRepository extends JpaRepository<Comision, Long> {
    // Puedes añadir métodos de consulta personalizados si los necesitas
    // Ejemplo: Buscar una comisión por el ID de transacción
    Optional<Comision> findByTransaccionId(Long transaccionId);
}