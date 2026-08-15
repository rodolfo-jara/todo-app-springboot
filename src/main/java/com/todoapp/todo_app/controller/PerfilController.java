package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.dto.PerfilResponse;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/perfil")
@SecurityRequirement(name = "bearerAuth")
public class PerfilController {

    private final AuthService authService;

    public PerfilController(AuthService authService) {
        this.authService = authService;
    }
    @GetMapping
    public ResponseEntity<?> perfil(Authentication authentication) {

        String email = authentication.getName();
        Usuario usuario = authService.buscarPorEmail(email);
        PerfilResponse perfilResponse = new PerfilResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol()
        );

        return ResponseEntity.ok(perfilResponse);
    }
    @GetMapping("/admin")
    public ResponseEntity<String> admin() {
        return ResponseEntity.ok("Bienvenido administrador");
    }
}