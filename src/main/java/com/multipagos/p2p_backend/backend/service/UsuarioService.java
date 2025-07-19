package com.multipagos.p2p_backend.backend.service; // Ajusta si tu paquete es diferente

import com.multipagos.p2p_backend.backend.model.Usuario; // Importa tu clase Usuario
import com.multipagos.p2p_backend.backend.repository.UsuarioRepository; // Importa tu UsuarioRepository
import org.springframework.security.crypto.password.PasswordEncoder; // Importa PasswordEncoder
import org.springframework.stereotype.Service; // Importa @Service
import org.springframework.transaction.annotation.Transactional; // Para manejo de transacciones

import java.time.LocalDateTime;
import java.util.Optional; // Para manejar Optional en findByEmail

@Service // Indica que esta es una clase de servicio de Spring
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder; // Inyectamos el PasswordEncoder

    // Constructor para inyección de dependencias (Spring lo hace automáticamente)
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario registrarUsuario(Usuario usuario) {
        // 1. Verificar si el email ya existe
        Optional<Usuario> existingUser = usuarioRepository.findByEmail(usuario.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }

        // 2. Hashear la contraseña antes de guardar
        String hashedPassword = passwordEncoder.encode(usuario.getPasswordHash());
        usuario.setPasswordHash(hashedPassword);

        // 3. Establecer valores por defecto para nuevos usuarios (si no los has establecido ya en @PrePersist o los quieres sobrescribir)
        if (usuario.getFechaRegistro() == null) {
            usuario.setFechaRegistro(LocalDateTime.now());
        }
        if (usuario.getEstado() == null) {
            usuario.setEstado("activo");
        }
        if (usuario.getAutenticador2faActivado() == null) {
            usuario.setAutenticador2faActivado(false);
        }
        if (usuario.getDisponibilidadCajero() == null) {
            // Solo un cajero puede estar disponible, por defecto no lo es para vendedor/admin
            // La lógica de asignación de rol y disponibilidad_cajero puede ser más compleja después
            usuario.setDisponibilidadCajero(false);
        }
        // Puedes establecer un 'creadoPor' si el registro lo hace un admin, o null/por defecto si es auto-registro
        usuario.setCreadoPor("SELF_REGISTER"); // Ejemplo
        usuario.setActualizadoEn(LocalDateTime.now()); // Asegurarse que se establece en el registro inicial también

        // 4. Guardar el usuario en la base de datos
        return usuarioRepository.save(usuario);
    }

    // Puedes añadir más métodos aquí, como encontrar un usuario por email para el login
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public boolean verificarCredenciales(String email, String password) {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);

        if (usuarioOptional.isEmpty()) {
            return false; // Usuario no encontrado
        }

        Usuario usuario = usuarioOptional.get();
        // Compara la contraseña en texto plano con la contraseña hasheada en la DB
        return passwordEncoder.matches(password, usuario.getPasswordHash());
    }

    @Transactional // Las operaciones que modifican la DB deben ser transaccionales
    public Usuario actualizarDisponibilidadCajero(Long userId, boolean disponible) {
        // Busca el usuario por su ID
        Usuario usuario = usuarioRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        // Verifica que el usuario tenga el rol de 'cajero'
        if (!usuario.getRol().equalsIgnoreCase("cajero")) {
            throw new IllegalArgumentException("Solo los usuarios con rol 'cajero' pueden cambiar su disponibilidad.");
        }

        // Actualiza el estado de disponibilidad del cajero
        usuario.setDisponibilidadCajero(disponible);
        // Guarda los cambios en la base de datos
        return usuarioRepository.save(usuario);
    }

    // --- ¡ESTE ES EL MÉTODO QUE NECESITAMOS RE-AÑADIR/ASEGURAR! ---
    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con email: " + email));
    }
    // -----------------------------------------------------------
}
