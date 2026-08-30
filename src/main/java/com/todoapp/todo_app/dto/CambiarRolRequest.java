package com.todoapp.todo_app.dto;

import com.todoapp.todo_app.entity.RolAplicacion;
import jakarta.validation.constraints.NotNull;

public class CambiarRolRequest {

    @NotNull
    private RolAplicacion rol;

    public RolAplicacion getRol() {
        return rol;
    }

    public void setRol(RolAplicacion rol) {
        this.rol = rol;
    }
}