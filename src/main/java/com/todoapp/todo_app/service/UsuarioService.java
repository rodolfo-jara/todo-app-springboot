package com.todoapp.todo_app.service;

import com.todoapp.todo_app.dto.RegistroRequest;
import com.todoapp.todo_app.entity.Aplicacion;
import com.todoapp.todo_app.entity.RolAplicacion;
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
    private final RefreshTokenService refreshTokenService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AplicacionRepository aplicacionRepository,
            UsuarioAplicacionRepository usuarioAplicacionRepository,
            RefreshTokenService refreshTokenService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.aplicacionRepository = aplicacionRepository;
        this.usuarioAplicacionRepository = usuarioAplicacionRepository;
        this.refreshTokenService = refreshTokenService;
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
                    RolAplicacion.USER
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

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));
    }

    public List<UsuarioAplicacion> listarAplicacionesDeUsuario(Long usuarioId) {

        buscarPorId(usuarioId);

        return usuarioAplicacionRepository
                .findByUsuarioId(usuarioId);
    }
    @Transactional
    public UsuarioAplicacion asignarAplicacion(
            Long usuarioId,
            Long aplicacionId
    ) {

        Usuario usuario = buscarPorId(usuarioId);

        Aplicacion aplicacion = aplicacionRepository.findById(aplicacionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Aplicación no encontrada"
                ));

        if (!aplicacion.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede asignar una aplicación inactiva"
            );
        }

        var accesoExistente =
                usuarioAplicacionRepository
                        .findByUsuarioIdAndAplicacionId(
                                usuarioId,
                                aplicacionId
                        );

        if (accesoExistente.isPresent()) {

            UsuarioAplicacion acceso = accesoExistente.get();

            if (acceso.isActivo()) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "El usuario ya tiene acceso a esta aplicación"
                );
            }

            acceso.setActivo(true);
            acceso.setRol(RolAplicacion.USER.name());

            return usuarioAplicacionRepository.save(acceso);
        }

        UsuarioAplicacion nuevoAcceso =
                new UsuarioAplicacion(
                        usuario,
                        aplicacion,
                        RolAplicacion.USER
                );

        return usuarioAplicacionRepository.save(nuevoAcceso);
    }
    @Transactional
    public UsuarioAplicacion quitarAplicacion(
            Long usuarioId,
            Long aplicacionId
    ) {

        Usuario usuario = buscarPorId(usuarioId);

        Aplicacion aplicacion = aplicacionRepository.findById(aplicacionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Aplicación no encontrada"
                ));

        UsuarioAplicacion acceso =
                usuarioAplicacionRepository
                        .findByUsuarioIdAndAplicacionId(
                                usuarioId,
                                aplicacionId
                        )
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "El usuario no tiene acceso a esta aplicación"
                        ));

        if (usuario.isSuperAdmin()
                && "auth-admin".equals(aplicacion.getCodigo())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede quitar auth-admin a un SUPER_ADMIN"
            );
        }

        acceso.setActivo(false);

        UsuarioAplicacion guardado =
                usuarioAplicacionRepository.save(acceso);

        refreshTokenService
                .revocarTodosPorUsuarioYAplicacion(
                        usuario,
                        aplicacion
                );

        return guardado;
    }
}