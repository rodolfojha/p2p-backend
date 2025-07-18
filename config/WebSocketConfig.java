package com.multipagos.p2p_backend.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
// Re-habilitar la importación y la inyección del interceptor
import org.springframework.messaging.simp.config.ChannelRegistration;
import com.multipagos.p2p_backend.backend.security.interceptor.JwtStompChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker // Habilita el manejo de mensajes WebSocket a través de un broker de mensajes STOMP
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // Re-habilitar la inyección del interceptor
    private final JwtStompChannelInterceptor jwtStompChannelInterceptor;

    // Re-habilitar el constructor con el interceptor
    public WebSocketConfig(JwtStompChannelInterceptor jwtStompChannelInterceptor) {
        this.jwtStompChannelInterceptor = jwtStompChannelInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Permitir cualquier origen para desarrollo
                .withSockJS();
    }

    // Re-habilitar el método de configuración del interceptor
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtStompChannelInterceptor);
    }
}
