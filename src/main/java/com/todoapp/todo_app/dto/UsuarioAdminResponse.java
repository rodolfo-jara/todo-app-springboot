package com.todoapp.todo_app.dto;

public class UsuarioAdminResponse {

    private Long id;
    private String nombre;
    private String email;
    private boolean superAdmin;

    public UsuarioAdminResponse(
            Long id,
            String nombre,
            String email,
            boolean superAdmin
    ) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.superAdmin = superAdmin;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }
}