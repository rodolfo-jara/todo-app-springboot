package com.todoapp.todo_app.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey key;

    private static final long EXPIRACION_MS = 15 * 60 * 1000;

    public JwtService(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    // Método nuevo: incluye si el usuario es SUPER_ADMIN global
    public String generarToken(
            String email,
            String rol,
            String app,
            boolean superAdmin
    ) {

        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .claim("superAdmin", superAdmin)
                .audience()
                .add(app)
                .and()
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRACION_MS
                        )
                )
                .signWith(key)
                .compact();
    }

    /*
     * Compatibilidad temporal.
     *
     * Lo mantenemos porque algunos tests todavía llaman
     * generarToken(email, rol, app).
     *
     * Esos tokens se consideran NO super admin.
     */
    public String generarToken(
            String email,
            String rol,
            String app
    ) {
        return generarToken(
                email,
                rol,
                app,
                false
        );
    }

    public String extraerEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean tokenValido(String token) {
        try {

            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public String extraerRol(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get(
                        "rol",
                        String.class
                );
    }

    public boolean extraerSuperAdmin(String token) {

        Boolean superAdmin = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get(
                        "superAdmin",
                        Boolean.class
                );

        return Boolean.TRUE.equals(superAdmin);
    }

    public String extraerApp(String token) {

        var audiencia = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getAudience();

        return audiencia.isEmpty()
                ? null
                : audiencia.iterator().next();
    }

    public String generarTokenExpiradoParaTest(
            String email,
            String rol,
            String app
    ) {

        Date ahora = new Date();

        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .claim("superAdmin", false)
                .audience()
                .add(app)
                .and()
                .issuedAt(
                        new Date(
                                ahora.getTime() - 120_000
                        )
                )
                .expiration(
                        new Date(
                                ahora.getTime() - 60_000
                        )
                )
                .signWith(key)
                .compact();
    }
}