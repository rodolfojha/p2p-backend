package com.multipagos.p2p_backend.backend.repository;

import com.multipagos.p2p_backend.backend.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Importar Query
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {
    // Ejemplo: Buscar transacciones por estado
    // Usamos FETCH JOIN para cargar vendedor y cajero junto con la transacción
    @Query("SELECT t FROM Transaccion t LEFT JOIN FETCH t.vendedor LEFT JOIN FETCH t.cajero WHERE t.estado = :estado")
    List<Transaccion> findByEstado(String estado);

    // Ejemplo: Buscar transacciones por vendedor ID
    @Query("SELECT t FROM Transaccion t LEFT JOIN FETCH t.vendedor LEFT JOIN FETCH t.cajero WHERE t.vendedor.id = :vendedorId")
    List<Transaccion> findByVendedorId(Long vendedorId);

    // Ejemplo: Buscar transacciones por cajero ID
    @Query("SELECT t FROM Transaccion t LEFT JOIN FETCH t.vendedor LEFT JOIN FETCH t.cajero WHERE t.cajero.id = :cajeroId")
    List<Transaccion> findByCajeroId(Long cajeroId);

    // Ejemplo: Buscar transacciones pendientes por tipo de operación (para que el cajero las vea)
    @Query("SELECT t FROM Transaccion t LEFT JOIN FETCH t.vendedor LEFT JOIN FETCH t.cajero WHERE t.estado = :estado AND t.tipoOperacion = :tipoOperacion")
    List<Transaccion> findByEstadoAndTipoOperacion(String estado, String tipoOperacion);

    // Buscar por UUID (el identificador público de la transacción)
    @Query("SELECT t FROM Transaccion t LEFT JOIN FETCH t.vendedor LEFT JOIN FETCH t.cajero WHERE t.uuid = :uuid")
    Optional<Transaccion> findByUuid(UUID uuid);

    // Método para encontrar transacciones por el email del vendedor
    @Query("SELECT t FROM Transaccion t LEFT JOIN FETCH t.vendedor LEFT JOIN FETCH t.cajero WHERE t.vendedor.email = :emailVendedor")
    List<Transaccion> findByVendedor_Email(String emailVendedor);

    // Método para encontrar transacciones por el email del cajero asignado
    @Query("SELECT t FROM Transaccion t LEFT JOIN FETCH t.vendedor LEFT JOIN FETCH t.cajero WHERE t.cajero.email = :emailCajero")
    List<Transaccion> findByCajero_Email(String emailCajero);
}
