package com.todoapp.todo_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AplicacionRequest {

    @NotBlank
    @Size(max = 50)
    @Pattern(
            regexp = "^\\s*[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*\\s*$",
            message = "El código solo puede contener letras, números y guiones"
    )
    private String codigo;

    @NotBlank
    @Size(max = 100)
    private String nombre;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}