package com.todoapp.todo_app.dto;

public class AplicacionResponse {

    private Long id;
    private String codigo;
    private String nombre;
    private boolean activo;

    public AplicacionResponse(
            Long id,
            String codigo,
            String nombre,
            boolean activo
    ) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public boolean isActivo() {
        return activo;
    }
}