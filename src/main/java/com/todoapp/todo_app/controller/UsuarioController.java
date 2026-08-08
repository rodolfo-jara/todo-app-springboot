package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }
    @PostMapping
    public ResponseEntity<Usuario> registrar(@RequestBody Usuario usuario){
        Usuario usuarioGuardado = usuarioService.guardarUsuario(usuario);
        return ResponseEntity.ok(usuarioGuardado);
    }
}