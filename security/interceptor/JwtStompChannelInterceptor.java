package com.multipagos.p2p_backend.backend.security.interceptor;

import com.multipagos.p2p_backend.backend.security.JwtProvider;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    public JwtStompChannelInterceptor(JwtProvider jwtProvider, UserDetailsService userDetailsService) {
        this.jwtProvider = jwtProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        try {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (accessor == null) {
                System.out.println("STOMP Interceptor: accessor nulo - permitiendo");
                return message;
            }

            StompCommand command = accessor.getCommand();
            System.out.println("STOMP Interceptor: Procesando comando " + command);

            if (StompCommand.CONNECT.equals(command)) {
                return handleConnectSafely(accessor, message);
            } else if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
                return handleOtherCommandsSafely(accessor, message);
            }
            
            // SIEMPRE permitir el mensaje
            return message;
            
        } catch (Exception e) {
            System.err.println("STOMP Interceptor: ERROR CRÍTICO: " + e.getMessage());
            e.printStackTrace();
            // EN CASO DE ERROR CRÍTICO, SIEMPRE PERMITIR EL MENSAJE
            return message;
        }
    }

    private Message<?> handleConnectSafely(StompHeaderAccessor accessor, Message<?> message) {
        try {
            System.out.println("STOMP Interceptor: === CONNECT SEGURO ===");
            
            List<String> authHeaders = accessor.getNativeHeader("Authorization");
            
            if (authHeaders == null || authHeaders.isEmpty()) {
                System.out.println("STOMP Interceptor: Sin token - conexión permitida sin auth");
                return message;
            }

            String authHeader = authHeaders.get(0);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                System.out.println("STOMP Interceptor: Header incorrecto - conexión permitida sin auth");
                return message;
            }

            String jwt = authHeader.substring(7);
            System.out.println("STOMP Interceptor: Validando token...");

            boolean isValid = jwtProvider.validateToken(jwt);
            System.out.println("STOMP Interceptor: Token válido: " + isValid);

            if (!isValid) {
                System.err.println("STOMP Interceptor: Token inválido - pero permitiendo conexión");
                return message;
            }

            String userEmail = jwtProvider.extractUsername(jwt);
            System.out.println("STOMP Interceptor: Email extraído: " + userEmail);

            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            
            accessor.setUser(authentication);
            System.out.println("STOMP Interceptor: ✅ Autenticación configurada para " + userEmail);
            
            return message;

        } catch (Exception e) {
            System.err.println("STOMP Interceptor: Error en CONNECT: " + e.getMessage());
            e.printStackTrace();
            System.err.println("STOMP Interceptor: Permitiendo conexión a pesar del error");
            return message;
        }
    }

    private Message<?> handleOtherCommandsSafely(StompHeaderAccessor accessor, Message<?> message) {
        try {
            Authentication authentication = (Authentication) accessor.getUser();
            if (authentication != null && authentication.isAuthenticated()) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("STOMP Interceptor: Auth configurado para " + accessor.getCommand() + ": " + authentication.getName());
            } else {
                System.out.println("STOMP Interceptor: Sin auth para " + accessor.getCommand());
            }
            return message;
        } catch (Exception e) {
            System.err.println("STOMP Interceptor: Error en " + accessor.getCommand() + ": " + e.getMessage());
            e.printStackTrace();
            return message;
        }
    }
}