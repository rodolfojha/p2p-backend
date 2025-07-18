package com.multipagos.p2p_backend.backend.dto; // Ajusta si tu paquete es diferente

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data // Genera getters, setters, equals, hashCode, toString
@NoArgsConstructor // Genera constructor sin argumentos
@AllArgsConstructor // Genera constructor con todos los argumentos
public class LoginRequest {
    private String email;
    private String password; // Contraseña en texto plano enviada por el usuario
}