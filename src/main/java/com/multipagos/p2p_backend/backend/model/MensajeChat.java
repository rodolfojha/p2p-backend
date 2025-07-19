package com.multipagos.p2p_backend.backend.model; // Ajusta si tu paquete es diferente

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes_chat") // Nombre de la tabla en la base de datos
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensajeChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Muchos mensajes para una transacción
    @JoinColumn(name = "id_transaccion", nullable = false)
    private Transaccion transaccion; // La transacción a la que pertenece este mensaje

    @ManyToOne // Muchos mensajes de un usuario
    @JoinColumn(name = "id_remitente", nullable = false)
    private Usuario remitente; // El usuario que envió el mensaje (vendedor o cajero)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido; // El texto del mensaje

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @PrePersist
    protected void onCreate() {
        this.fechaEnvio = LocalDateTime.now();
    }
}