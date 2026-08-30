package com.todoapp.todo_app.repository;

import com.todoapp.todo_app.entity.Aplicacion;
import com.todoapp.todo_app.entity.RefreshToken;
import com.todoapp.todo_app.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    long deleteByRevocadoTrue();

    long deleteByExpiraEnBefore(Instant ahora);

    long deleteByAplicacionIsNull();

    @Modifying
    @Query("""
            UPDATE RefreshToken rt
            SET rt.revocado = true
            WHERE rt.aplicacion = :aplicacion
              AND rt.revocado = false
            """)
    int revocarTodosPorAplicacion(
            @Param("aplicacion") Aplicacion aplicacion
    );
    @Modifying
    @Query("""
        UPDATE RefreshToken rt
        SET rt.revocado = true
        WHERE rt.usuario = :usuario
          AND rt.aplicacion = :aplicacion
          AND rt.revocado = false
        """)
    int revocarTodosPorUsuarioYAplicacion(
            @Param("usuario") Usuario usuario,
            @Param("aplicacion") Aplicacion aplicacion
    );
}