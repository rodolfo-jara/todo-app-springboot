package com.todoapp.todo_app.dto;

import jakarta.validation.constraints.NotNull;

public class AplicacionEstadoRequest {

    @NotNull
    private Boolean activo;

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}