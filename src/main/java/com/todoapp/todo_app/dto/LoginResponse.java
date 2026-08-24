package com.todoapp.todo_app.dto;

public class LoginResponse {

    private String token;
    private String refreshToken;
    private PerfilResponse usuario;

    public LoginResponse(String token, String refreshToken, PerfilResponse usuario) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.usuario = usuario;
    }

    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public PerfilResponse getUsuario() { return usuario; }
}