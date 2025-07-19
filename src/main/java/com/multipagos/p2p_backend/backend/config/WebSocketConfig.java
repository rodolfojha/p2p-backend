package com.multipagos.p2p_backend.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.ChannelRegistration;
import com.multipagos.p2p_backend.backend.security.interceptor.HeaderCaptureInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final HeaderCaptureInterceptor headerCaptureInterceptor;

    public WebSocketConfig(HeaderCaptureInterceptor headerCaptureInterceptor) {
        this.headerCaptureInterceptor = headerCaptureInterceptor;
        System.out.println("=== WebSocketConfig con interceptor simple ===");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        System.out.println("=== Configurando Message Broker ===");
        config.enableSimpleBroker("/topic", "/user");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        System.out.println("=== Registrando STOMP endpoints ===");
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        System.out.println("=== Configurando interceptor simple ===");
        registration.interceptors(headerCaptureInterceptor);
    }
}