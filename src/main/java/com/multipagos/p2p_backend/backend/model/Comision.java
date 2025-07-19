package com.multipagos.p2p_backend.backend.model; // Ajusta si tu paquete es diferente

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "comisiones") // Asegúrate de que coincida con el nombre de tu tabla
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne // Relación Uno a Uno: una comisión por transacción (siempre)
    @JoinColumn(name = "id_transaccion", nullable = false, unique = true) // Clave foránea, debe ser única
    private Transaccion transaccion;

    @Column(name = "monto_comision_bruta", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoComisionBruta;

    @Column(name = "porcentaje_plataforma", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajePlataforma;

    @Column(name = "monto_plataforma", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoPlataforma;

    @Column(name = "porcentaje_vendedor", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeVendedor;

    @Column(name = "monto_vendedor", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoVendedor;

    @Column(name = "porcentaje_cajero", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeCajero;

    @Column(name = "monto_cajero", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoCajero;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}