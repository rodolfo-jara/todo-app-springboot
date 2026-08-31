package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.dto.AgregarUsuarioAppRequest;
import com.todoapp.todo_app.dto.UsuarioAppAdminResponse;
import com.todoapp.todo_app.entity.UsuarioAplicacion;
import com.todoapp.todo_app.service.UsuarioService;
import jakarta.validation.Valid;
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
    @PostMapping("/usuarios")
    public ResponseEntity<UsuarioAppAdminResponse> agregarUsuario(
            Authentication authentication,
            @RequestHeader("X-App-Id") String appCodigo,
            @Valid @RequestBody AgregarUsuarioAppRequest request
    ) {

        UsuarioAplicacion acceso =
                usuarioService.agregarUsuarioAPropiaAplicacion(
                        authentication.getName(),
                        appCodigo,
                        request.getEmail()
                );

        UsuarioAppAdminResponse response =
                new UsuarioAppAdminResponse(
                        acceso.getUsuario().getId(),
                        acceso.getUsuario().getNombre(),
                        acceso.getUsuario().getEmail(),
                        acceso.getRol(),
                        acceso.isActivo()
                );

        return ResponseEntity.status(201).body(response);
    }
    @DeleteMapping("/usuarios/{usuarioId}")
    public ResponseEntity<UsuarioAppAdminResponse> quitarUsuario(
            Authentication authentication,
            @RequestHeader("X-App-Id") String appCodigo,
            @PathVariable Long usuarioId
    ) {

        UsuarioAplicacion acceso =
                usuarioService.quitarUsuarioDePropiaAplicacion(
                        authentication.getName(),
                        appCodigo,
                        usuarioId
                );

        UsuarioAppAdminResponse response =
                new UsuarioAppAdminResponse(
                        acceso.getUsuario().getId(),
                        acceso.getUsuario().getNombre(),
                        acceso.getUsuario().getEmail(),
                        acceso.getRol(),
                        acceso.isActivo()
                );

        return ResponseEntity.ok(response);
    }
}