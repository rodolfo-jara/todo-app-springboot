package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.dto.LoginRequest;
import com.todoapp.todo_app.dto.LoginResponse;
import com.todoapp.todo_app.dto.PerfilResponse;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.service.AuthService;
import com.todoapp.todo_app.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            AuthService authService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @GetMapping("/usuario")
    public ResponseEntity<?> buscarUsuario(@RequestParam String email) {

        Usuario usuario = authService.buscarPorEmail(email);

        if (usuario == null) {
            return ResponseEntity.status(404)
                    .body("Usuario no encontrado");
        }

        PerfilResponse perfil = new PerfilResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol()
        );

        return ResponseEntity.ok(perfil);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

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

        String token = jwtService.generarToken(
                usuario.getEmail(),
                usuario.getRol());

        PerfilResponse perfil = new PerfilResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol()
        );

        LoginResponse response = new LoginResponse(
                token,
                perfil
        );

        return ResponseEntity.ok(response);
    }

}