package com.multipagos.p2p_backend.backend.dto; // Ajusta si tu paquete es diferente

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal; // Importa BigDecimal

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudTransaccionRequest {

    @NotBlank(message = "El tipo de operación (depósito/retiro) es obligatorio.")
    private String tipoOperacion; // "deposito" o "retiro"

    @NotNull(message = "El monto es obligatorio.")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero.")
    private BigDecimal monto;

    @NotNull(message = "El ID del método de pago del vendedor es obligatorio.")
    private Long metodoPagoVendedorId;

    @NotBlank(message = "La moneda es obligatoria.")
    private String moneda; // Ej: "VES", "USD", "COP"

    // Campo para la opción de comisión:
    // "restar" (del total recibido) o "agregar" (al total del cliente)
    @NotBlank(message = "La opción de comisión es obligatoria (restar/agregar).")
    private String opcionComision;
}