    package com.multipagos.p2p_backend.backend.dto; // Ajusta si tu paquete es diferente

    import lombok.Data;
    import lombok.NoArgsConstructor;
    import lombok.AllArgsConstructor;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class CrearDisputaRequest {

        @NotNull(message = "El ID de la transacción es obligatorio.")
        private Long transaccionId;

        @NotBlank(message = "El motivo de la disputa es obligatorio.")
        @Size(min = 10, max = 1000, message = "El motivo debe tener entre 10 y 1000 caracteres.")
        private String motivoDisputa;

        // Opcional: URL a evidencia adicional (comprobantes, capturas)
        private String urlEvidenciaAdicional;
    }
    