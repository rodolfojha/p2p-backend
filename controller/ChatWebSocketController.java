package com.multipagos.p2p_backend.backend.controller;

import com.multipagos.p2p_backend.backend.model.MensajeChat;
import com.multipagos.p2p_backend.backend.model.Usuario;
import com.multipagos.p2p_backend.backend.service.ChatService;
import com.multipagos.p2p_backend.backend.service.UsuarioService;
import com.multipagos.p2p_backend.backend.security.JwtProvider;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UsuarioService usuarioService;
    private final JwtProvider jwtProvider;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   ChatService chatService,
                                   UsuarioService usuarioService,
                                   JwtProvider jwtProvider) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.usuarioService = usuarioService;
        this.jwtProvider = jwtProvider;
    }

    @MessageMapping("/chat/{transaccionId}")
    public void processChatMessage(
        @DestinationVariable Long transaccionId,
        @Payload Map<String, Object> payloadData,
        SimpMessageHeaderAccessor headerAccessor
    ) {
        String contenidoMensaje = (String) payloadData.get("contenido");

        System.out.println("=== MENSAJE RECIBIDO CON VALIDACIÓN MANUAL ===");
        System.out.println("Transacción ID: " + transaccionId);
        System.out.println("Contenido: " + contenidoMensaje);

        try {
            // VALIDACIÓN MANUAL DEL JWT
            Usuario remitente = validarJWTYObtenerUsuario(headerAccessor);
            
            if (remitente == null) {
                System.err.println("❌ Usuario no autenticado - rechazando mensaje");
                enviarMensajeError(transaccionId, "Usuario no autenticado");
                return;
            }

            System.out.println("✅ Usuario autenticado: " + remitente.getNombreCompleto() + " (ID: " + remitente.getId() + ")");

            // Guardar mensaje en la base de datos
            MensajeChat mensajeGuardado = chatService.guardarMensaje(transaccionId, remitente.getId(), contenidoMensaje);
            System.out.println("✅ Mensaje guardado en DB con ID: " + mensajeGuardado.getId());

            // Crear respuesta con datos reales del usuario y mensaje guardado
            Map<String, Object> response = new HashMap<>();
            response.put("id", mensajeGuardado.getId());
            response.put("contenido", mensajeGuardado.getContenido());
            response.put("transaccionId", transaccionId);
            response.put("timestamp", mensajeGuardado.getFechaEnvio().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            response.put("remitente", Map.of(
                "id", remitente.getId(),
                "nombreCompleto", remitente.getNombreCompleto(),
                "rol", remitente.getRol()
            ));
            
            messagingTemplate.convertAndSend("/topic/chat/" + transaccionId, response);
            System.out.println("✅ Mensaje real enviado a todos los suscriptores");
            
        } catch (Exception e) {
            System.err.println("❌ Error al procesar mensaje: " + e.getMessage());
            e.printStackTrace();
            enviarMensajeError(transaccionId, "Error al procesar mensaje: " + e.getMessage());
        }
        
        System.out.println("=== FIN PROCESAMIENTO ===");
    }

    private Usuario validarJWTYObtenerUsuario(SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Obtener headers nativos de la sesión STOMP
            @SuppressWarnings("unchecked")
            Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) headerAccessor.getSessionAttributes().get("nativeHeaders");
            
            if (nativeHeaders == null) {
                System.err.println("❌ No hay headers nativos en la sesión");
                return null;
            }

            List<String> authHeaders = nativeHeaders.get("Authorization");
            if (authHeaders == null || authHeaders.isEmpty()) {
                System.err.println("❌ No hay header Authorization en la sesión");
                return null;
            }

            String authHeader = authHeaders.get(0);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.err.println("❌ Header Authorization no es Bearer");
                return null;
            }

            String jwt = authHeader.substring(7);
            System.out.println("✅ JWT extraído de la sesión, validando...");

            if (!jwtProvider.validateToken(jwt)) {
                System.err.println("❌ Token JWT inválido");
                return null;
            }

            String userEmail = jwtProvider.extractUsername(jwt);
            System.out.println("✅ Token válido para email: " + userEmail);

            return usuarioService.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + userEmail));

        } catch (Exception e) {
            System.err.println("❌ Error en validación JWT: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void enviarMensajeError(Long transaccionId, String mensaje) {
        Map<String, Object> errorMessage = new HashMap<>();
        errorMessage.put("error", true);
        errorMessage.put("mensaje", mensaje);
        errorMessage.put("timestamp", LocalDateTime.now().toString());
        
        messagingTemplate.convertAndSend("/topic/chat/" + transaccionId, errorMessage);
    }

    // Mantener el método simple para testing
    @MessageMapping("/test")
    public void testMessage(@Payload String message) {
        System.out.println("=== TEST MESSAGE ===");
        System.out.println("Mensaje: " + message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("contenido", "Echo: " + message);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("remitente", Map.of("nombreCompleto", "Sistema Test", "rol", "sistema"));
        
        messagingTemplate.convertAndSend("/topic/test", response);
    }
}