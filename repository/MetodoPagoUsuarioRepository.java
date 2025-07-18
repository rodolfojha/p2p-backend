package com.multipagos.p2p_backend.backend.repository; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.MetodoPagoUsuario; // Importa tu clase de modelo
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // Para métodos que devuelven listas
import java.util.Optional; // Para métodos que devuelven un solo resultado opcional

public interface MetodoPagoUsuarioRepository extends JpaRepository<MetodoPagoUsuario, Long> {
    // Puedes añadir métodos de consulta personalizados aquí si los necesitas
    // Ejemplo: Buscar métodos de pago por usuario y estado
    List<MetodoPagoUsuario> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    // Ejemplo: Buscar un método de pago específico por usuario y ID del método
    Optional<MetodoPagoUsuario> findByIdAndUsuarioId(Long id, Long usuarioId);
}