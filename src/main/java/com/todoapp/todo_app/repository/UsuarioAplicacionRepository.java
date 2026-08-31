package com.todoapp.todo_app.repository;

import com.todoapp.todo_app.entity.RolAplicacion;
import com.todoapp.todo_app.entity.UsuarioAplicacion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioAplicacionRepository
        extends JpaRepository<UsuarioAplicacion, Long> {

    @EntityGraph(attributePaths = {"usuario", "aplicacion"})
    Optional<UsuarioAplicacion>
    findByUsuarioEmailAndAplicacionCodigo(
            String email,
            String codigo
    );

    List<UsuarioAplicacion> findByUsuarioId(
            Long usuarioId
    );

    Optional<UsuarioAplicacion> findByUsuarioIdAndAplicacionId(
            Long usuarioId,
            Long aplicacionId
    );

    List<UsuarioAplicacion> findByAplicacionCodigo(
            String codigo
    );

    long countByAplicacionIdAndRolAndActivoTrue(
            Long aplicacionId,
            RolAplicacion rol
    );
}