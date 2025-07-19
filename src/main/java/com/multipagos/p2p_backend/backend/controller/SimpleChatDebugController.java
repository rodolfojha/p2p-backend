/* package com.multipagos.p2p_backend.backend.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
public class SimpleChatDebugController {

    private final SimpMessagingTemplate messagingTemplate;

    public SimpleChatDebugController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/test")
    public void testMessage(@Payload String message) {
        System.out.println("=== TEST MESSAGE RECIBIDO ===");
        System.out.println("Mensaje: " + message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("contenido", "Echo: " + message);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("remitente", Map.of("nombreCompleto", "Sistema Debug", "rol", "sistema"));
        
        messagingTemplate.convertAndSend("/topic/test", response);
        System.out.println("Respuesta enviada a /topic/test");
    }

    @MessageMapping("/chat/{transaccionId}")
    public void debugChatMessage(
        @DestinationVariable Long transaccionId,
        @Payload String message
    ) {
        System.out.println("=== DEBUG CHAT MESSAGE ===");
        System.out.println("Transacción ID: " + transaccionId);
        System.out.println("Mensaje: " + message);
        
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("contenido", message);
            response.put("transaccionId", transaccionId);
            response.put("timestamp", LocalDateTime.now().toString());
            response.put("remitente", Map.of(
                "nombreCompleto", "Debug User",
                "rol", "DEBUG"
            ));
            
            String destination = "/topic/chat/" + transaccionId;
            messagingTemplate.convertAndSend(destination, response);
            System.out.println("✅ Mensaje enviado a: " + destination);
            
        } catch (Exception e) {
            System.err.println("❌ Error al procesar mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

*/