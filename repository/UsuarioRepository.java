// --- Contenido de UsuarioRepository.java ---
package com.multipagos.p2p_backend.backend.repository; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.Usuario; // O similar, la que usabas antes // Importa tu clase Usuario
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email); // Método para buscar usuarios por email
}