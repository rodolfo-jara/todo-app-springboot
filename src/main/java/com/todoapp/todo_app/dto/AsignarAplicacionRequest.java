package com.todoapp.todo_app.dto;

import jakarta.validation.constraints.NotNull;

public class AsignarAplicacionRequest {

    @NotNull
    private Long aplicacionId;

    public Long getAplicacionId() {
        return aplicacionId;
    }

    public void setAplicacionId(Long aplicacionId) {
        this.aplicacionId = aplicacionId;
    }
}