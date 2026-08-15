package com.todoapp.todo_app.service;

import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario guardarUsuario(Usuario usuario) {

        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );
        usuario.setRol("USER");
        return usuarioRepository.save(usuario);
    }
    public Iterable<Usuario> listarUsuariso() {
        return usuarioRepository.findAll();
    }
}
