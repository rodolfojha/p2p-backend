package com.multipagos.p2p_backend.backend.dto; // Ajusta si tu paquete es diferente

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoUsuarioRequest {

    @NotBlank(message = "El tipo de cuenta es obligatorio.")
    private String tipoCuenta; // Ej: 'ahorros', 'corriente', 'nequi', 'daviplata', 'bancolombia'

    @NotBlank(message = "El número de cuenta es obligatorio.")
    @Size(min = 5, max = 50, message = "El número de cuenta debe tener entre 5 y 50 caracteres.")
    private String numeroCuenta;

    @NotBlank(message = "El nombre del titular es obligatorio.")
    @Size(min = 3, max = 255, message = "El nombre del titular debe tener entre 3 y 255 caracteres.")
    private String nombreTitular;

    // Opcional: validación de identificación si es un campo sensible
    // @Pattern(regexp = "^[0-9]{7,15}$", message = "La identificación debe ser numérica y tener entre 7 y 15 dígitos.")
    private String identificacionTitular;

    private String aliasMetodo; // Nombre amigable
}