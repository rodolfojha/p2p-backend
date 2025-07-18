package com.multipagos.p2p_backend.backend.model; // Ajusta si tu paquete es diferente

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal; // Para el tipo DECIMAL
import java.time.LocalDateTime;
import java.util.UUID; // Para el tipo UUID

@Entity
@Table(name = "transacciones") // Asegúrate de que coincida con el nombre de tu tabla
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID uuid; // ID único para referencia externa

    @ManyToOne
    @JoinColumn(name = "id_vendedor", nullable = false)
    private Usuario vendedor; // Usuario que inició la solicitud

    @ManyToOne
    @JoinColumn(name = "id_cajero") // Puede ser NULL al inicio
    private Usuario cajero; // Usuario que aceptó la solicitud

    @Column(name = "tipo_operacion", nullable = false)
    private String tipoOperacion; // 'deposito' o 'retiro'

    @Column(nullable = false, precision = 18, scale = 2) // Precision y escala para DECIMAL
    private BigDecimal monto;

    @Column(nullable = false, length = 10) // Longitud de la cadena para la moneda
    private String moneda; // Ej: 'VES', 'USD', 'COP'

    @Column(name = "comision_bruta", nullable = false, precision = 18, scale = 2)
    private BigDecimal comisionBruta;

    @Column(name = "monto_neto_vendedor", nullable = false, precision = 18, scale = 2)
    private BigDecimal montoNetoVendedor;

    @Column(nullable = false)
    private String estado; // 'pendiente', 'aceptada', 'en_proceso_pago', 'en_proceso_confirmacion', 'completada', 'cancelada', 'disputa'

    @ManyToOne
    @JoinColumn(name = "metodo_pago_vendedor_id")
    private MetodoPagoUsuario metodoPagoVendedor;

    @ManyToOne
    @JoinColumn(name = "metodo_pago_cajero_id")
    private MetodoPagoUsuario metodoPagoCajero;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_aceptacion")
    private LocalDateTime fechaAceptacion;

    @Column(name = "fecha_pago_iniciado")
    private LocalDateTime fechaPagoIniciado;

    @Column(name = "fecha_confirmacion_final")
    private LocalDateTime fechaConfirmacionFinal;

    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;

    @Column(name = "url_comprobante_pago")
    private String urlComprobantePago; // URL al comprobante

    @Column(name = "notas_transaccion", columnDefinition = "TEXT") // TEXT para campos largos
    private String notasTransaccion;

    @Column(name = "creado_en")
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    protected void onCreate() {
        this.uuid = UUID.randomUUID(); // Genera un UUID al crear
        this.fechaSolicitud = LocalDateTime.now();
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
        if (this.estado == null || this.estado.isEmpty()) {
            this.estado = "pendiente"; // Estado inicial por defecto
        }
        if (this.moneda == null || this.moneda.isEmpty()) {
            this.moneda = "VES"; // Moneda por defecto
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}