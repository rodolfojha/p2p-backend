package com.multipagos.p2p_backend.backend.repository; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.Transaccion; // Importa tu clase de modelo
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // Para buscar por UUID

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    // Ejemplo: Buscar transacciones por estado
    List<Transaccion> findByEstado(String estado);

    // Ejemplo: Buscar transacciones por vendedor
    List<Transaccion> findByVendedorId(Long vendedorId);

    // Ejemplo: Buscar transacciones por cajero
    List<Transaccion> findByCajeroId(Long cajeroId);

    // Ejemplo: Buscar transacciones pendientes por tipo de operación (para que el cajero las vea)
    List<Transaccion> findByEstadoAndTipoOperacion(String estado, String tipoOperacion);

    // Buscar por UUID (el identificador público de la transacción)
    Optional<Transaccion> findByUuid(UUID uuid);
}