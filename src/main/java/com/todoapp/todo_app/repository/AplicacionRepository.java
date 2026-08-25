package com.todoapp.todo_app.repository;

import com.todoapp.todo_app.entity.Aplicacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AplicacionRepository extends JpaRepository<Aplicacion, Long> {

    Optional<Aplicacion> findByCodigo(String codigo);
}