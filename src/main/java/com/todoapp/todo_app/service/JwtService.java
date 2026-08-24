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

    public JwtService(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static final long EXPIRACION_MS = 15 * 60 * 1000;

    public String generarToken(String email, String rol, String app) {

        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .audience().add(app).and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACION_MS))
                .signWith(key)
                .compact();
    }

    public String extraerEmail(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean tokenValido(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extraerRol(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().get("rol", String.class);
    }

    public String extraerApp(String token) {
        var audiencia = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getAudience();
        return audiencia.isEmpty() ? null : audiencia.iterator().next();
    }
}