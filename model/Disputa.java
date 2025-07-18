package com.multipagos.p2p_backend.backend.model; // Ajusta si tu paquete es diferente

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "disputas") // Asegúrate de que coincida con el nombre de tu tabla
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Disputa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "id_transaccion", nullable = false, unique = true)
    private Transaccion transaccion; // La transacción en disputa

    @ManyToOne
    @JoinColumn(name = "id_usuario_reporta", nullable = false)
    private Usuario usuarioReporta; // El usuario que inició la disputa

    @Column(name = "motivo_disputa", nullable = false, columnDefinition = "TEXT")
    private String motivoDisputa;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    private String estado; // 'abierta', 'en_revision', 'resuelta_vendedor', 'resuelta_cajero', 'cancelada'

    @ManyToOne
    @JoinColumn(name = "id_administrador_asignado")
    private Usuario administradorAsignado; // Administrador a cargo, puede ser NULL

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "decision_administrador", columnDefinition = "TEXT")
    private String decisionAdministrador;

    @Column(name = "url_evidencia_adicional", columnDefinition = "TEXT")
    private String urlEvidenciaAdicional;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        this.fechaInicio = LocalDateTime.now();
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
        if (this.estado == null || this.estado.isEmpty()) {
            this.estado = "abierta"; // Estado inicial por defecto
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}