package com.todoapp.todo_app.service;

import com.todoapp.todo_app.entity.Aplicacion;
import com.todoapp.todo_app.entity.RefreshToken;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.entity.UsuarioAplicacion;
import com.todoapp.todo_app.repository.RefreshTokenRepository;
import com.todoapp.todo_app.repository.UsuarioAplicacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private static final Duration DURACION = Duration.ofDays(30);

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioAplicacionRepository usuarioAplicacionRepository;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UsuarioAplicacionRepository usuarioAplicacionRepository
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioAplicacionRepository = usuarioAplicacionRepository;
    }

    public record RotacionResultado(
            Usuario usuario,
            UsuarioAplicacion acceso,
            String nuevoRefreshToken
    ) {
    }

    public String crear(
            Usuario usuario,
            Aplicacion aplicacion
    ) {

        String tokenClaro = generarTokenAleatorio();

        RefreshToken rt = new RefreshToken();

        rt.setUsuario(usuario);
        rt.setAplicacion(aplicacion);
        rt.setTokenHash(hash(tokenClaro));
        rt.setCreadoEn(Instant.now());
        rt.setExpiraEn(Instant.now().plus(DURACION));
        rt.setRevocado(false);

        refreshTokenRepository.save(rt);

        return tokenClaro;
    }

    @Transactional
    public Optional<RotacionResultado> rotar(
            String tokenClaro,
            String appCodigo
    ) {

        Optional<RefreshToken> opt =
                refreshTokenRepository.findByTokenHash(
                        hash(tokenClaro)
                );

        if (opt.isEmpty()) {
            return Optional.empty();
        }

        RefreshToken rt = opt.get();

        // Token revocado o expirado
        if (rt.isRevocado()
                || rt.getExpiraEn().isBefore(Instant.now())) {

            return Optional.empty();
        }

        Aplicacion aplicacion = rt.getAplicacion();

        // Los refresh tokens antiguos que todavía no tienen app
        // dejan de ser válidos.
        if (aplicacion == null) {
            return Optional.empty();
        }

        if (appCodigo == null || appCodigo.isBlank()) {
            return Optional.empty();
        }

        String appNormalizada = appCodigo
                .trim()
                .toLowerCase(Locale.ROOT);

        // El refresh token solo sirve para la app
        // para la cual fue emitido.
        if (!aplicacion.getCodigo().equals(appNormalizada)) {
            return Optional.empty();
        }

        // Si toda la aplicación está desactivada,
        // tampoco se permite renovar.
        if (!aplicacion.isActivo()) {
            return Optional.empty();
        }

        Usuario usuario = rt.getUsuario();

        // Volvemos a comprobar que el usuario siga teniendo
        // acceso activo a esa aplicación.
        UsuarioAplicacion acceso =
                usuarioAplicacionRepository
                        .findByUsuarioEmailAndAplicacionCodigo(
                                usuario.getEmail(),
                                aplicacion.getCodigo()
                        )
                        .orElse(null);

        if (acceso == null || !acceso.isActivo()) {
            return Optional.empty();
        }

        // Solo después de todas las validaciones
        // revocamos el token anterior.
        rt.setRevocado(true);
        refreshTokenRepository.save(rt);

        // El nuevo refresh queda ligado a LA MISMA app.
        String nuevo = crear(
                usuario,
                aplicacion
        );

        return Optional.of(
                new RotacionResultado(
                        usuario,
                        acceso,
                        nuevo
                )
        );
    }

    public void revocar(String tokenClaro) {

        refreshTokenRepository
                .findByTokenHash(hash(tokenClaro))
                .ifPresent(rt -> {

                    rt.setRevocado(true);

                    refreshTokenRepository.save(rt);
                });
    }
    @Transactional
    public int revocarTodosPorAplicacion(Aplicacion aplicacion) {
        return refreshTokenRepository
                .revocarTodosPorAplicacion(aplicacion);
    }
    private String generarTokenAleatorio() {

        byte[] bytes = new byte[32];

        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hash(String tokenClaro) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(
                    tokenClaro.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getEncoder()
                    .encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(e);
        }
    }

    @Transactional
    public long limpiarTokens() {

        long sinAplicacion =
                refreshTokenRepository.deleteByAplicacionIsNull();

        long revocados =
                refreshTokenRepository.deleteByRevocadoTrue();

        long expirados =
                refreshTokenRepository.deleteByExpiraEnBefore(
                        Instant.now()
                );

        return sinAplicacion + revocados + expirados;
    }
    @Transactional
    public int revocarTodosPorUsuarioYAplicacion(
            Usuario usuario,
            Aplicacion aplicacion
    ) {
        return refreshTokenRepository
                .revocarTodosPorUsuarioYAplicacion(
                        usuario,
                        aplicacion
                );
    }
}