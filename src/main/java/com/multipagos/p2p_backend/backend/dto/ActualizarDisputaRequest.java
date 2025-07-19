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
    public class ActualizarDisputaRequest {

        @NotBlank(message = "El estado de la disputa es obligatorio.")
        private String estado; // Ej: 'en_revision', 'resuelta_vendedor', 'resuelta_cajero', 'cancelada'

        @NotBlank(message = "La decisión del administrador es obligatoria.")
        @Size(min = 10, max = 2000, message = "La decisión debe tener entre 10 y 2000 caracteres.")
        private String decisionAdministrador;

        // Opcional: ID del administrador que resuelve la disputa (se obtendría del JWT en producción)
        @NotNull(message = "El ID del administrador es obligatorio.")
        private Long administradorId;
    }
    