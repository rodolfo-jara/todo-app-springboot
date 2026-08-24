package com.todoapp.todo_app.service;

import com.todoapp.todo_app.entity.RefreshToken;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class RefreshTokenService {

    // Duración del refresh token. Ajusta según qué tan larga quieres
    // que sea la sesión "de fondo" antes de forzar un login nuevo.
    private static final Duration DURACION = Duration.ofDays(30);

    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public record RotacionResultado(Usuario usuario, String nuevoRefreshToken) {}

    public String crear(Usuario usuario) {
        String tokenClaro = generarTokenAleatorio();

        RefreshToken rt = new RefreshToken();
        rt.setUsuario(usuario);
        rt.setTokenHash(hash(tokenClaro));
        rt.setCreadoEn(Instant.now());
        rt.setExpiraEn(Instant.now().plus(DURACION));
        rt.setRevocado(false);

        refreshTokenRepository.save(rt);

        // El valor en claro solo existe aquí; en BD solo queda el hash.
        return tokenClaro;
    }

    // Valida el refresh token recibido, lo revoca y entrega uno nuevo
    // (rotación: cada refresh token se usa una sola vez).
    public Optional<RotacionResultado> rotar(String tokenClaro) {

        Optional<RefreshToken> opt = refreshTokenRepository.findByTokenHash(hash(tokenClaro));
        if (opt.isEmpty()) {
            return Optional.empty();
        }

        RefreshToken rt = opt.get();
        if (rt.isRevocado() || rt.getExpiraEn().isBefore(Instant.now())) {
            return Optional.empty();
        }

        rt.setRevocado(true);
        refreshTokenRepository.save(rt);

        String nuevo = crear(rt.getUsuario());
        return Optional.of(new RotacionResultado(rt.getUsuario(), nuevo));
    }

    public void revocar(String tokenClaro) {
        refreshTokenRepository.findByTokenHash(hash(tokenClaro))
                .ifPresent(rt -> {
                    rt.setRevocado(true);
                    refreshTokenRepository.save(rt);
                });
    }

    private String generarTokenAleatorio() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    // SHA-256 simple (no BCrypt): el refresh token ya es aleatorio de alta
    // entropía (no elegido por un humano), así que no necesita salt/costo;
    // solo buscamos que una fuga de la BD no exponga tokens usables.
    private String hash(String tokenClaro) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(tokenClaro.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}