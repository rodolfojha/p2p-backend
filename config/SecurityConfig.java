package com.multipagos.p2p_backend.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import com.multipagos.p2p_backend.backend.security.filter.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.messaging.Message;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.messaging.simp.stomp.StompCommand; // Keep this import if needed elsewhere, but not for simpType(StompCommand.CONNECT)


@Configuration
@EnableMethodSecurity // Habilitar seguridad a nivel de método (@PreAuthorize)
@EnableWebSocketSecurity // Habilitar seguridad a nivel de WebSocket (STOMP)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // Permite cualquier origen
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*")); // Permite cualquier encabezado
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization")); // Exponer Authorization header

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilita CSRF
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // HABILITA CORS
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas: WebSocket handshake, recursos estáticos, registro/login
                .requestMatchers(
                    "/ws/**", // WebSocket endpoint (handshake HTTP)
                    "/", "/stomp-client.html", "/js/**", "/css/**", "/images/**", "/favicon.ico", // Recursos estáticos
                    "/api/auth/register", "/api/auth/login" // Endpoints de autenticación
                ).permitAll() // Permitir acceso público a estas rutas
                // AHORA SÍ, TODAS LAS DEMÁS PETICIONES REQUIEREN AUTENTICACIÓN
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin sesiones
            .authenticationProvider(authenticationProvider()) // Re-habilitar AuthenticationProvider
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint())) // Re-habilitar AuthenticationEntryPoint
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Re-habilitar filtro JWT

        return http.build();
    }

    // Re-habilitar la configuración de seguridad a nivel de mensaje STOMP
    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager() {
        MessageMatcherDelegatingAuthorizationManager.Builder messages = MessageMatcherDelegatingAuthorizationManager.builder();
        
        // ELIMINAMOS ESTA LÍNEA: messages.simpType(StompCommand.CONNECT).permitAll(); 
        // El comando CONNECT es manejado por el interceptor y el resto de reglas.

        // Permite que los usuarios autenticados envíen mensajes a /app/chat/{transaccionId}
        messages
            .simpDestMatchers("/app/chat/**").authenticated() // Solo usuarios autenticados pueden ENVIAR a /app/chat
            // Permite que los usuarios autenticados se suscriban a /topic/chat/{transaccionId}
            .simpSubscribeDestMatchers("/topic/chat/**").authenticated() // Solo usuarios autenticados pueden SUSCRIBIRSE al chat
            // Permite que cualquier usuario se suscriba a los tópicos públicos (ej. solicitudes pendientes para cajeros)
            .simpSubscribeDestMatchers("/topic/solicitudes-pendientes").permitAll()
            .simpSubscribeDestMatchers("/topic/solicitudes-actualizadas").permitAll()
            // Denegar cualquier otro mensaje STOMP por defecto si no coincide con las reglas anteriores
            .anyMessage().denyAll();
        return messages.build();
    }
}
