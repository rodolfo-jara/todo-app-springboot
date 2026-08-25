package com.todoapp.todo_app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public class RegistroRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
    private String password;

    // Nombre de la app en la que se está registrando (ej. "gastos", "todo-app")
    @NotBlank(message = "El campo app es obligatorio")
    private String app;

    public RegistroRequest() {
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email == null
                ? null
                : email.trim().toLowerCase(Locale.ROOT);
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getApp() { return app; }
    public void setApp(String app) { this.app = app; }
}