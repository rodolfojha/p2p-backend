package com.multipagos.p2p_backend.backend.repository; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.Disputa; // Importa tu clase de modelo
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DisputaRepository extends JpaRepository<Disputa, Long> {
    // Puedes añadir métodos de consulta personalizados aquí
    // Ejemplo: Buscar disputas por estado
    List<Disputa> findByEstado(String estado);

    // Ejemplo: Buscar disputas por la transacción involucrada
    Optional<Disputa> findByTransaccionId(Long transaccionId);

    // Ejemplo: Buscar disputas reportadas por un usuario
    List<Disputa> findByUsuarioReportaId(Long usuarioReportaId);

    // Ejemplo: Buscar disputas asignadas a un administrador
    List<Disputa> findByAdministradorAsignadoId(Long administradorAsignadoId);
}