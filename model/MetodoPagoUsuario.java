package com.multipagos.p2p_backend.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore; // <-- NUEVA IMPORTACIÓN

import java.time.LocalDateTime;

@Entity
@Table(name = "metodos_pago_usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    @JsonIgnore // <-- ¡AÑADE ESTA ANOTACIÓN AQUÍ!
    private Usuario usuario; // El usuario al que pertenece este método de pago


    @Column(name = "tipo_cuenta", nullable = false)
    private String tipoCuenta; // Ej: 'ahorros', 'corriente', 'nequi', 'daviplata', 'bancolombia'

    @Column(name = "numero_cuenta", nullable = false)
    private String numeroCuenta; // Número de cuenta o teléfono

    @Column(name = "nombre_titular", nullable = false)
    private String nombreTitular;

    @Column(name = "identificacion_titular")
    private String identificacionTitular; // Cédula/DNI

    @Column(name = "alias_metodo")
    private String aliasMetodo; // Nombre amigable (ej. "Mi Nequi")

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    private String estado; // 'activo', 'inactivo', 'verificado', 'pendiente_verificacion'

    private String notas; // Notas para el administrador

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        if (this.estado == null || this.estado.isEmpty()) {
            this.estado = "activo"; // Por defecto activo, o "pendiente_verificacion" si se requiere
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}