package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.dto.*;
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
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioAdminResponse> buscarUsuarioPorId(
            @PathVariable Long id
    ) {

        Usuario usuario = usuarioService.buscarPorId(id);

        UsuarioAdminResponse response = new UsuarioAdminResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.isSuperAdmin()
        );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}/aplicaciones")
    public ResponseEntity<List<UsuarioAplicacionResponse>>
    listarAplicacionesDeUsuario(
            @PathVariable Long id
    ) {

        List<UsuarioAplicacionResponse> aplicaciones =
                usuarioService.listarAplicacionesDeUsuario(id)
                        .stream()
                        .map(acceso -> new UsuarioAplicacionResponse(
                                acceso.getAplicacion().getId(),
                                acceso.getAplicacion().getCodigo(),
                                acceso.getAplicacion().getNombre(),
                                acceso.getRol(),
                                acceso.isActivo()
                        ))
                        .toList();

        return ResponseEntity.ok(aplicaciones);
    }
    @PostMapping("/{id}/aplicaciones")
    public ResponseEntity<UsuarioAplicacionResponse> asignarAplicacion(
            @PathVariable Long id,
            @Valid @RequestBody AsignarAplicacionRequest request
    ) {

        UsuarioAplicacion acceso =
                usuarioService.asignarAplicacion(
                        id,
                        request.getAplicacionId()
                );

        UsuarioAplicacionResponse response =
                new UsuarioAplicacionResponse(
                        acceso.getAplicacion().getId(),
                        acceso.getAplicacion().getCodigo(),
                        acceso.getAplicacion().getNombre(),
                        acceso.getRol(),
                        acceso.isActivo()
                );

        return ResponseEntity.status(201).body(response);
    }

    @DeleteMapping("/{usuarioId}/aplicaciones/{aplicacionId}")
    public ResponseEntity<UsuarioAplicacionResponse> quitarAplicacion(
            @PathVariable Long usuarioId,
            @PathVariable Long aplicacionId
    ) {

        UsuarioAplicacion acceso =
                usuarioService.quitarAplicacion(
                        usuarioId,
                        aplicacionId
                );

        UsuarioAplicacionResponse response =
                new UsuarioAplicacionResponse(
                        acceso.getAplicacion().getId(),
                        acceso.getAplicacion().getCodigo(),
                        acceso.getAplicacion().getNombre(),
                        acceso.getRol(),
                        acceso.isActivo()
                );

        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{usuarioId}/aplicaciones/{aplicacionId}/rol")
    public ResponseEntity<UsuarioAplicacionResponse> cambiarRol(
            @PathVariable Long usuarioId,
            @PathVariable Long aplicacionId,
            @Valid @RequestBody CambiarRolRequest request
    ) {

        UsuarioAplicacion acceso =
                usuarioService.cambiarRol(
                        usuarioId,
                        aplicacionId,
                        request.getRol()
                );

        UsuarioAplicacionResponse response =
                new UsuarioAplicacionResponse(
                        acceso.getAplicacion().getId(),
                        acceso.getAplicacion().getCodigo(),
                        acceso.getAplicacion().getNombre(),
                        acceso.getRol(),
                        acceso.isActivo()
                );

        return ResponseEntity.ok(response);
    }
}