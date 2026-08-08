package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.dto.LoginRequest;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
            AuthService authService,
            PasswordEncoder passwordEncoder
    ) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/usuario")
    public ResponseEntity<?> buscarUsuario(@RequestParam String email) {

        Usuario usuario = authService.buscarPorEmail(email);

        if (usuario == null) {
            return ResponseEntity.status(404)
                    .body("Usuario no encontrado");
        }

        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest request) {

        Usuario usuario = authService.buscarPorEmail(request.getEmail());

        if (usuario == null) {
            return ResponseEntity.status(404)
                    .body("Usuario no encontrado");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                usuario.getPassword()
        )) {
            return ResponseEntity.status(401)
                    .body("Contraseña incorrecta");
        }

        return ResponseEntity.ok(
                "Login correcto. Bienvenido: " + usuario.getNombre()
        );
    }
}