package com.todoapp.todo_app.dto;

public class UsuarioAplicacionResponse {

    private Long aplicacionId;
    private String codigo;
    private String nombre;
    private String rol;
    private boolean activo;

    public UsuarioAplicacionResponse(
            Long aplicacionId,
            String codigo,
            String nombre,
            String rol,
            boolean activo
    ) {
        this.aplicacionId = aplicacionId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.rol = rol;
        this.activo = activo;
    }

    public Long getAplicacionId() {
        return aplicacionId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}
