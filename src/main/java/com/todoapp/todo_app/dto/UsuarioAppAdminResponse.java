package com.todoapp.todo_app.dto;

public class UsuarioAppAdminResponse {

    private Long usuarioId;
    private String nombre;
    private String email;
    private String rol;
    private boolean activo;

    public UsuarioAppAdminResponse(
            Long usuarioId,
            String nombre,
            String email,
            String rol,
            boolean activo
    ) {
        this.usuarioId = usuarioId;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.activo = activo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }
}