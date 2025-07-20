package com.multipagos.p2p_backend.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transacciones")
@Data
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid; // Identificador público de la transacción

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor", nullable = false)
    private Usuario vendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cajero") // Puede ser nulo al inicio
    private Usuario cajero;

    @Column(nullable = false)
    private String tipoOperacion; // "deposito" o "retiro"

    @Column(nullable = false)
    private BigDecimal monto;

    @Column(nullable = false)
    private String moneda; // Ej: "USD", "VES"

    @Column(nullable = false)
    private BigDecimal comisionBruta;

    @Column(nullable = false)
    private BigDecimal montoNetoVendedor;

    @Column(nullable = false)
    private String estado; // "pendiente", "aceptada", "en_proceso_pago", "en_proceso_confirmacion", "completada", "cancelada", "disputa"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_pago_vendedor_id", nullable = false)
    private MetodoPagoUsuario metodoPagoVendedor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_pago_cajero_id") // Puede ser nulo al inicio
    private MetodoPagoUsuario metodoPagoCajero;

    @Column(nullable = false)
    private LocalDateTime fechaSolicitud;

    private LocalDateTime fechaAceptacion;
    private LocalDateTime fechaPagoIniciado;
    private LocalDateTime fechaConfirmacionFinal;
    private LocalDateTime fechaExpiracion; // Para transacciones con tiempo límite

    // CAMBIO IMPORTANTE AQUÍ: Usar columnDefinition = "TEXT" para almacenar cadenas muy largas
    @Column(name = "url_comprobante_pago", columnDefinition = "TEXT")
    private String urlComprobantePago;

    @Column(name = "notas_transaccion")
    private String notasTransaccion;

    // Constructor para inicializar UUID y fechaSolicitud
    @PrePersist
    protected void onCreate() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
        if (this.fechaSolicitud == null) {
            this.fechaSolicitud = LocalDateTime.now();
        }
    }
}
