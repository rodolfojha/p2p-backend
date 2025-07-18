package com.multipagos.p2p_backend.backend.controller;

import com.multipagos.p2p_backend.backend.model.MensajeChat;
import com.multipagos.p2p_backend.backend.model.Usuario;
import com.multipagos.p2p_backend.backend.service.ChatService;
import com.multipagos.p2p_backend.backend.service.UsuarioService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate; // <-- ¡AÑADIR ESTA IMPORTACIÓN!
import org.springframework.security.core.Authentication; // <-- ¡RE-HABILITAR ESTA IMPORTACIÓN!
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;

@Controller
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final UsuarioService usuarioService;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate,
                                   ChatService chatService,
                                   UsuarioService usuarioService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.usuarioService = usuarioService;
    }

    // Método principal para manejar mensajes de chat (ahora siempre en modo desarrollo)
    @MessageMapping("/chat/{transaccionId}")
    public void processChatMessage(
        @DestinationVariable Long transaccionId,
        @Payload Map<String, Object> payloadData, // Recibir el JSON como Map
        Authentication authentication // <-- ¡RE-HABILITAR ESTE PARÁMETRO!
    ) {
        // Extraer el contenido real del mensaje del payload
        String contenidoMensaje = (String) payloadData.get("contenido"); // <-- Ahora 'contenidoMensaje' está definido

        System.out.println("=== MENSAJE RECIBIDO (MODO DEV - SIN SEGURIDAD) ===");
        System.out.println("Transacción ID: " + transaccionId);
        System.out.println("Contenido: " + contenidoMensaje); // Ahora debería ser el texto limpio
        System.out.println("Authentication (en este modo): " + (authentication != null ? authentication.getName() : "null")); // <-- Ahora 'authentication' está definido

        try {
            // MODO DESARROLLO: Sin autenticación, usar un remitente por defecto y no guardar en DB
            System.out.println("⚠️ Modo desarrollo - sin autenticación (no se guarda en DB)");
            
            Map<String, Object> tempMessage = new HashMap<>();
            tempMessage.put("id", System.currentTimeMillis()); // ID temporal para el cliente
            tempMessage.put("contenido", contenidoMensaje);
            tempMessage.put("transaccionId", transaccionId);
            tempMessage.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            tempMessage.put("remitente", Map.of(
                "id", 999L, // ID temporal
                "nombreCompleto", "Usuario Desarrollo", // Nombre temporal
                "rol", "DEV" // Rol temporal
            ));
            
            messagingTemplate.convertAndSend("/topic/chat/" + transaccionId, tempMessage);
            System.out.println("Mensaje temporal reenviado.");
            
        } catch (Exception e) {
            System.err.println("❌ Error al procesar mensaje de chat: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("error", true);
            errorMessage.put("mensaje", "Error al procesar mensaje: " + e.getMessage());
            errorMessage.put("timestamp", LocalDateTime.now().toString());
            
            messagingTemplate.convertAndSend("/topic/chat/" + transaccionId, errorMessage);
        }
        
        System.out.println("=== FIN PROCESAMIENTO ===");
    }

    // Este método es un endpoint alternativo para pruebas simples, se mantiene si es necesario para otros clientes
    @MessageMapping("/chat-simple/{transaccionId}")
    public void processChatMessageSimple(
        @DestinationVariable Long transaccionId,
        @Payload Map<String, Object> message
        // Eliminamos Authentication authentication
    ) {
        System.out.println("=== MENSAJE SIMPLE RECIBIDO (MODO DEV) ===");
        System.out.println("Data: " + message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("contenido", (String) message.get("contenido")); 
        response.put("remitente", Map.of( 
            "nombreCompleto", (String) message.get("usuario"),
            "rol", (String) message.get("rol")
        ));
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        response.put("transaccionId", transaccionId);
        
        messagingTemplate.convertAndSend("/topic/chat/" + transaccionId, response);
        System.out.println("Mensaje simple reenviado");
    }
}
