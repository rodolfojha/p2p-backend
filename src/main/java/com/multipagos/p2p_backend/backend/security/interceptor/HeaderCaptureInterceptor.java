package com.multipagos.p2p_backend.backend.security.interceptor;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class HeaderCaptureInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                // Capturar headers nativos y guardarlos en la sesión
                Map<String, List<String>> nativeHeaders = accessor.toNativeHeaderMap();
                if (nativeHeaders != null && !nativeHeaders.isEmpty()) {
                    accessor.getSessionAttributes().put("nativeHeaders", nativeHeaders);
                    System.out.println("✅ Headers capturados en sesión: " + nativeHeaders.keySet());
                }
            }
            
            return message;
            
        } catch (Exception e) {
            System.err.println("❌ Error capturando headers: " + e.getMessage());
            e.printStackTrace();
            return message; // Siempre permitir
        }
    }
}