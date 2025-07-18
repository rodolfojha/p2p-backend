// --- Contenido de Usuario.java ---
package com.multipagos.p2p_backend.backend.model; // Ajusta si tu paquete es diferente

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios") // Asegúrate de que el nombre de la tabla sea 'usuarios'
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo", nullable = false) // Usar nombre de columna específico si difiere del camelCase
    private String nombreCompleto;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String rol; // "vendedor", "cajero", "administrador"

    private String telefono;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "ultimo_login")
    private LocalDateTime ultimoLogin;

    private String estado; // "activo", "inactivo", "bloqueado"

    @Column(name = "autenticador_2fa_secreto")
    private String autenticador2faSecreto;

    @Column(name = "autenticador_2fa_activado")
    private Boolean autenticador2faActivado;

    @Column(name = "disponibilidad_cajero")
    private Boolean disponibilidadCajero;

    @Column(name = "creado_por")
    private String creadoPor;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.estado == null) { // Solo si no se establece explícitamente
            this.estado = "activo";
        }
        if (this.autenticador2faActivado == null) {
            this.autenticador2faActivado = false;
        }
        if (this.disponibilidadCajero == null) {
            this.disponibilidadCajero = false;
        }
        this.actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}