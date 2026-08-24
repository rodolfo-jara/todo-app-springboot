package com.todoapp.todo_app.dto;

import jakarta.validation.constraints.NotBlank;

public class RefreshRequest {

    @NotBlank(message = "El refreshToken es obligatorio")
    private String refreshToken;

    @NotBlank(message = "El campo app es obligatorio")
    private String app;

    public RefreshRequest() {
    }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getApp() { return app; }
    public void setApp(String app) { this.app = app; }
}