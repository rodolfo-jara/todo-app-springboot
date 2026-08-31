package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.dto.UsuarioAppAdminResponse;
import com.todoapp.todo_app.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminAplicacionController {

    private final UsuarioService usuarioService;

    public AdminAplicacionController(
            UsuarioService usuarioService
    ) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioAppAdminResponse>> listarUsuarios(
            Authentication authentication,
            @RequestHeader("X-App-Id") String appCodigo
    ) {

        List<UsuarioAppAdminResponse> usuarios =
                usuarioService.listarUsuariosDeAplicacion(
                        authentication.getName(),
                        appCodigo
                );

        return ResponseEntity.ok(usuarios);
    }
}