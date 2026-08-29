package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.dto.PerfilResponse;
import com.todoapp.todo_app.dto.RegistroRequest;
import com.todoapp.todo_app.dto.UsuarioAdminResponse;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.entity.UsuarioAplicacion;
import com.todoapp.todo_app.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {

        this.usuarioService = usuarioService;
    }


    // Público: solo puede crear un usuario nuevo con rol USER.
    // RegistroRequest no tiene campos "id" ni "rol", así que el cliente
    // no puede sobrescribir otras cuentas ni auto-asignarse ADMIN.

    @PostMapping
    public ResponseEntity<PerfilResponse> registrar(
            @Valid @RequestBody RegistroRequest request
    ) {

        UsuarioAplicacion acceso = usuarioService.registrar(request);

        Usuario usuario = acceso.getUsuario();

        PerfilResponse perfil = new PerfilResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                acceso.getRol()
        );

        return ResponseEntity.status(201).body(perfil);
    }

    // Solo ADMIN puede listar usuarios (ver SecurityConfig).
    @GetMapping
    public ResponseEntity<List<UsuarioAdminResponse>> listarUsuarios() {

        List<UsuarioAdminResponse> usuarios =
                usuarioService.listarUsuarios()
                        .stream()
                        .map(u -> new UsuarioAdminResponse(
                                u.getId(),
                                u.getNombre(),
                                u.getEmail(),
                                u.isSuperAdmin()
                        ))
                        .toList();

        return ResponseEntity.ok(usuarios);
    }
}