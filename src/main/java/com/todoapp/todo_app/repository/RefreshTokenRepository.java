package com.todoapp.todo_app.repository;

import com.todoapp.todo_app.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    long deleteByRevocadoTrue();

    long deleteByExpiraEnBefore(Instant ahora);

    long deleteByAplicacionIsNull();
}