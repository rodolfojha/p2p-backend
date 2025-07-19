package com.multipagos.p2p_backend.backend.security; // Ajusta si tu paquete es diferente

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component // Para que Spring lo gestione como un bean
public class JwtProvider {

    @Value("${jwt.secret}") // La clave secreta para firmar los tokens (se define en application.properties)
    private String secret;

    @Value("${jwt.expiration}") // Tiempo de expiración del token en milisegundos
    private long expiration;

    // --- Método para generar un token JWT ---
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
            .claims(claims) // Nuevo método para claims
            .subject(email) // Nuevo método para subject
            .issuedAt(new Date(System.currentTimeMillis())) // Nuevo método para issuedAt
            .expiration(new Date(System.currentTimeMillis() + expiration)) // Nuevo método para expiration
            .signWith(getSignKey()) // Firma con la clave directamente (el algoritmo se infiere de la clave)
            .compact();
    }

    // --- Métodos para extraer información del token ---

    // Extrae un claim específico del token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extrae todos los claims del token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSignKey()) // <-- NUEVO: usa verifyWith en lugar de setSigningKey
            .build()
            .parseSignedClaims(token) // <-- NUEVO: usa parseSignedClaims
            .getPayload(); // <-- NUEVO: usa getPayload en lugar de getBody
    }
    
    // Extrae el email del token (el 'subject')
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extrae la fecha de expiración del token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // --- Método para validar si el token es válido ---
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token); // <-- NUEVO: usa verifyWith y parseSignedClaims
            return true;
        } catch (Exception e) {
            System.err.println("Error de validación JWT: " + e.getMessage());
            // Puedes ser más específico aquí, por ejemplo:
            // if (e instanceof ExpiredJwtException) {
            //     System.err.println("Token JWT expirado.");
            // } else if (e instanceof SignatureException) {
            //     System.err.println("Firma JWT inválida.");
            // }
            return false;
        }
    }

    // Obtiene la clave de firma a partir del secreto base64
    private SecretKey getSignKey() { // <-- CAMBIAR Key a SecretKey
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // --- MÉTODO DE DEBUG AGREGADO ---
    public void debugToken(String token) {
        try {
            System.out.println("=== JWT DEBUG ===");
            System.out.println("Token recibido: " + (token != null ? token.substring(0, Math.min(50, token.length())) + "..." : "null"));
            
            if (token != null) {
                String username = extractUsername(token);
                Date expiration = extractExpiration(token);
                boolean isValid = validateToken(token);
                
                System.out.println("Username extraído: " + username);
                System.out.println("Expiración: " + expiration);
                System.out.println("Es válido: " + isValid);
                System.out.println("Fecha actual: " + new Date());
                
                // Extraer rol si existe
                Claims claims = extractAllClaims(token);
                Object role = claims.get("role");
                System.out.println("Rol extraído: " + role);
            }
            System.out.println("=== FIN JWT DEBUG ===");
        } catch (Exception e) {
            System.err.println("Error en debug JWT: " + e.getMessage());
            e.printStackTrace();
        }
    }
}