package com.multipagos.p2p_backend.backend.security.interceptor; // Ajusta si tu paquete es diferente

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
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // System.out.println("--- STOMP Interceptor: Comando " + accessor.getCommand() + " ---"); // Log general

        // Intercepta el comando CONNECT (cuando el cliente se conecta por primera vez)
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // System.out.println("STOMP Interceptor: Procesando CONNECT command."); // Log
            List<String> authorizationHeaders = accessor.getNativeHeader("Authorization");
            String jwt = null;

            if (authorizationHeaders != null && !authorizationHeaders.isEmpty()) {
                String authHeader = authorizationHeaders.get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    jwt = authHeader.substring(7);
                    // System.out.println("STOMP Interceptor: JWT extraído: " + jwt.substring(0, Math.min(20, jwt.length())) + "..."); // Log (solo inicio del token)
                } else {
                    System.out.println("STOMP Interceptor: Encabezado Authorization no Bearer o vacío."); // Log
                }
            } else {
                System.out.println("STOMP Interceptor: Encabezado Authorization ausente."); // Log
            }

            if (jwt != null) {
                if (jwtProvider.validateToken(jwt)) {
                    String userEmail = jwtProvider.extractUsername(jwt);
                    // System.out.println("STOMP Interceptor: Token válido para email: " + userEmail); // Log
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                    if (userDetails != null) {
                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        // ¡CRUCIAL! Establece la autenticación en el contexto de seguridad para esta sesión STOMP
                        accessor.setUser(authentication); // Asocia la autenticación con la sesión STOMP
                        System.out.println("STOMP Interceptor: Usuario " + userEmail + " autenticado para WebSocket."); // Log
                    } else {
                        System.err.println("STOMP Interceptor: UserDetails nulo para email: " + userEmail); // Log de error
                    }
                } else {
                    System.err.println("STOMP Interceptor: Token JWT inválido."); // Log de error
                }
            } else {
                System.err.println("STOMP Interceptor: JWT es nulo. Conexión no autenticada."); // Log de error
            }
        } else if (StompCommand.SEND.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            // System.out.println("STOMP Interceptor: Procesando comando " + accessor.getCommand() + "."); // Log
            Authentication authentication = (Authentication) accessor.getUser();
            if (authentication != null && authentication.isAuthenticated()) {
                // Establece la autenticación en el SecurityContextHolder para que los controladores @MessageMapping puedan acceder a ella.
                SecurityContextHolder.getContext().setAuthentication(authentication);
                // System.out.println("STOMP Interceptor: Mensaje/Suscripción de usuario autenticado: " + authentication.getName()); // Log
            } else {
                System.err.println("STOMP Interceptor: Mensaje/Suscripción de usuario NO autenticado en la sesión STOMP. Denegando."); // Log de error
                // Si no está autenticado, denegar el mensaje/suscripción
                // Esto se maneja por AuthorizationChannelInterceptor y messageAuthorizationManager
                // throw new MessageDeliveryException("Unauthorized STOMP message/subscription");
            }
        }
        return message;
    }
}
