package com.todoapp.todo_app.service;

import com.todoapp.todo_app.dto.RegistroRequest;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

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

    public Usuario registrar(RegistroRequest request) {
        String emailNormalizado = request.getEmail()
                .trim()
                .toLowerCase();

        var existente = usuarioRepository.findByEmail(emailNormalizado);

        if (existente.isPresent()) {
            Usuario usuario = existente.get();

            // Ya existe una cuenta con ese email: para sumarle acceso a
            // esta nueva app, exigimos la contraseña correcta. Así nadie
            // puede "registrarse" con el email de otra persona para
            // ganar acceso sin ser el dueño de la cuenta.
            if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
            }

            usuario.getAplicaciones().add(request.getApp());
            return usuarioRepository.save(usuario);
        }

        // Usuario completamente nuevo.
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(emailNormalizado);
        usuario.setPassword(
                passwordEncoder.encode(request.getPassword()));
        usuario.setRol("USER");
        usuario.getAplicaciones().add(request.getApp());

        try {
            return usuarioRepository.save(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya está registrado");
        }
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}