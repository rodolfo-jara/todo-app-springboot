package com.todoapp.todo_app.service;

import com.todoapp.todo_app.dto.RegistroRequest;
import com.todoapp.todo_app.entity.Aplicacion;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.entity.UsuarioAplicacion;
import com.todoapp.todo_app.repository.AplicacionRepository;
import com.todoapp.todo_app.repository.UsuarioAplicacionRepository;
import com.todoapp.todo_app.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AplicacionRepository aplicacionRepository;
    private final UsuarioAplicacionRepository usuarioAplicacionRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AplicacionRepository aplicacionRepository,
            UsuarioAplicacionRepository usuarioAplicacionRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.aplicacionRepository = aplicacionRepository;
        this.usuarioAplicacionRepository = usuarioAplicacionRepository;
    }
    @Transactional
    public UsuarioAplicacion registrar(RegistroRequest request) {

        String emailNormalizado = request.getEmail()
                .trim()
                .toLowerCase();

        // 1. Verificar que la aplicación exista
        Aplicacion aplicacion = aplicacionRepository
                .findByCodigo(request.getApp())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Aplicación no válida"
                ));

        // 2. Verificar que la aplicación esté activa
        if (!aplicacion.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Aplicación no disponible"
            );
        }

        // 3. El email no puede registrarse nuevamente
        if (usuarioRepository.findByEmail(emailNormalizado).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El email ya está registrado"
            );
        }

        // 4. Crear el usuario
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(emailNormalizado);
        usuario.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        try {

            Usuario usuarioGuardado =
                    usuarioRepository.save(usuario);

            // 5. Crear acceso del usuario a la aplicación
            UsuarioAplicacion acceso = new UsuarioAplicacion(
                    usuarioGuardado,
                    aplicacion,
                    "USER"
            );

            // 6. La fuente de verdad ahora es UsuarioAplicacion
            return usuarioAplicacionRepository.save(acceso);

        } catch (DataIntegrityViolationException e) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El email ya está registrado"
            );
        }
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}