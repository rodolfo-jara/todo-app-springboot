package com.todoapp.todo_app.dto;

public class LoginResponse {

    private String token;
    private PerfilResponse usuario;

    public LoginResponse(String token, PerfilResponse usuario) {
        this.token = token;
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public PerfilResponse getUsuario() {
        return usuario;
    }
}