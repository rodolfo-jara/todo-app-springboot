package com.todoapp.todo_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Locale;

public class LoginRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    // A qué app se está intentando loguear (ej. "gastos", "todo-app")
    @NotBlank(message = "El campo app es obligatorio")
    private String app;

    public LoginRequest() {
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email == null
                ? null
                : email.trim().toLowerCase(Locale.ROOT);
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getApp() { return app; }
    public void setApp(String app) {
        this.app = app == null
                ? null
                : app.trim().toLowerCase(Locale.ROOT);
    }
}