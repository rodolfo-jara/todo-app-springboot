package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.dto.PerfilResponse;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.entity.UsuarioAplicacion;
import com.todoapp.todo_app.repository.UsuarioAplicacionRepository;
import com.todoapp.todo_app.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/perfil")
@SecurityRequirement(name = "bearerAuth")
public class PerfilController {

    private final AuthService authService;
    private final UsuarioAplicacionRepository usuarioAplicacionRepository;

    public PerfilController(
            AuthService authService,
            UsuarioAplicacionRepository usuarioAplicacionRepository
    ) {
        this.authService = authService;
        this.usuarioAplicacionRepository = usuarioAplicacionRepository;
    }

    @GetMapping
    public ResponseEntity<PerfilResponse> perfil(
            Authentication authentication,
            @RequestHeader("X-App-Id") String app
    ) {

        String email = authentication.getName();

        Usuario usuario = authService.buscarPorEmail(email);

        UsuarioAplicacion acceso = usuarioAplicacionRepository
                .findByUsuarioEmailAndAplicacionCodigo(
                        email,
                        app
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "No tienes acceso a esta aplicación"
                ));

        if (!acceso.isActivo()
                || !acceso.getAplicacion().isActivo()) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes acceso a esta aplicación"
            );
        }

        PerfilResponse perfilResponse = new PerfilResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                acceso.getRol()
        );

        return ResponseEntity.ok(perfilResponse);
    }

}