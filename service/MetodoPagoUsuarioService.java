package com.multipagos.p2p_backend.backend.service; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.MetodoPagoUsuario;
import com.multipagos.p2p_backend.backend.model.Usuario;
import com.multipagos.p2p_backend.backend.repository.MetodoPagoUsuarioRepository;
import com.multipagos.p2p_backend.backend.repository.UsuarioRepository;
import com.multipagos.p2p_backend.backend.dto.MetodoPagoUsuarioRequest; // Importa el DTO
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Para manejo de transacciones
import java.util.List;
import java.util.Optional;

@Service
public class MetodoPagoUsuarioService {

    private final MetodoPagoUsuarioRepository metodoPagoUsuarioRepository;
    private final UsuarioRepository usuarioRepository; // Necesitamos el UsuarioRepository para buscar usuarios

    public MetodoPagoUsuarioService(MetodoPagoUsuarioRepository metodoPagoUsuarioRepository, UsuarioRepository usuarioRepository) {
        this.metodoPagoUsuarioRepository = metodoPagoUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional // Las operaciones que modifican la DB deben ser transaccionales
    public MetodoPagoUsuario crearMetodoPago(Long userId, MetodoPagoUsuarioRequest request) {
        // 1. Verificar que el usuario exista
        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        // 2. Crear la entidad MetodoPagoUsuario a partir del DTO
        MetodoPagoUsuario metodoPago = new MetodoPagoUsuario();
        metodoPago.setUsuario(usuario);
        metodoPago.setTipoCuenta(request.getTipoCuenta());
        metodoPago.setNumeroCuenta(request.getNumeroCuenta());
        metodoPago.setNombreTitular(request.getNombreTitular());
        metodoPago.setIdentificacionTitular(request.getIdentificacionTitular());
        metodoPago.setAliasMetodo(request.getAliasMetodo());
        metodoPago.setEstado("activo"); // Puedes poner "pendiente_verificacion" si quieres un flujo de aprobación

        // 3. Guardar en la base de datos
        return metodoPagoUsuarioRepository.save(metodoPago);

        
    }

    @Transactional(readOnly = true) // Solo lectura
    public List<MetodoPagoUsuario> obtenerMetodosPagoPorUsuario(Long userId) {
        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));
        return metodoPagoUsuarioRepository.findByUsuarioIdAndEstado(usuario.getId(), "activo"); // <-- Usando usuario.getId() // Solo activos por ejemplo
    }

    @Transactional(readOnly = true)
    public Optional<MetodoPagoUsuario> obtenerMetodoPagoPorIdYUsuario(Long metodoPagoId, Long userId) {
        return metodoPagoUsuarioRepository.findByIdAndUsuarioId(metodoPagoId, userId);
    }

    // --- NUEVO MÉTODO A AÑADIR AQUÍ ---
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));
    }
}