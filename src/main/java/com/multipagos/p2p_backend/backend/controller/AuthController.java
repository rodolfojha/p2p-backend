package com.multipagos.p2p_backend.backend.controller;

import com.multipagos.p2p_backend.backend.model.Usuario;
import com.multipagos.p2p_backend.backend.service.UsuarioService;
import com.multipagos.p2p_backend.backend.dto.LoginRequest;
import com.multipagos.p2p_backend.backend.security.JwtProvider; // <-- Asegúrate de que esto está importado
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.HashMap; // <-- Asegúrate de que esto está importado
import java.util.Map;     // <-- Asegúrate de que esto está importado

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtProvider jwtProvider; // <-- Asegúrate de que esto está declarado

    // Constructor para inyección de dependencias
    public AuthController(UsuarioService usuarioService, JwtProvider jwtProvider) { // <-- Asegúrate de que JwtProvider está en el constructor
        this.usuarioService = usuarioService;
        this.jwtProvider = jwtProvider; // <-- Asegúrate de que se asigna
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Usuario nuevoUsuario) {
        try {
            if (nuevoUsuario.getEmail() == null || nuevoUsuario.getEmail().isEmpty() ||
                nuevoUsuario.getPasswordHash() == null || nuevoUsuario.getPasswordHash().isEmpty() ||
                nuevoUsuario.getRol() == null || nuevoUsuario.getRol().isEmpty()) {
                return ResponseEntity.badRequest().body("Email, contraseña y rol son campos obligatorios.");
            }

            if (!nuevoUsuario.getRol().equalsIgnoreCase("vendedor") &&
                !nuevoUsuario.getRol().equalsIgnoreCase("cajero") &&
                !nuevoUsuario.getRol().equalsIgnoreCase("administrador")) {
                return ResponseEntity.badRequest().body("Rol inválido. Los roles permitidos son 'vendedor', 'cajero', 'administrador'.");
            }

            Usuario registradoUsuario = usuarioService.registrarUsuario(nuevoUsuario);
            registradoUsuario.setPasswordHash(null);
            return new ResponseEntity<>(registradoUsuario, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar el usuario: " + e.getMessage());
        }
    }

     @GetMapping("/protected-test") // Este endpoint estará protegido
    public ResponseEntity<String> protectedTest() {
        return ResponseEntity.ok("Acceso concedido a recurso protegido. ¡Autenticación JWT exitosa!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty() ||
                loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body("Email y contraseña son obligatorios.");
            }

            boolean credencialesValidas = usuarioService.verificarCredenciales(
                loginRequest.getEmail(), loginRequest.getPassword()
            );

            if (credencialesValidas) {
                Optional<Usuario> usuarioOptional = usuarioService.findByEmail(loginRequest.getEmail());
                if (usuarioOptional.isPresent()) { // <-- ¡ASEGÚRATE DE QUE ESTE IF ESTÉ AHÍ Y LA LÓGICA DENTRO!
                    Usuario usuario = usuarioOptional.get();
                    // Generar el token JWT
                    String token = jwtProvider.generateToken(usuario.getEmail(), usuario.getRol()); // <-- ESTO ES CLAVE

                    Map<String, String> response = new HashMap<>();
                    response.put("token", token);
                    response.put("message", "Login exitoso");
                    response.put("role", usuario.getRol());

                    return ResponseEntity.ok(response); // <-- DEVOLVER EL MAP CON EL TOKEN
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario no encontrado a pesar de credenciales válidas.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas.");
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al iniciar sesión: " + e.getMessage());
        }
    }
}