package com.todoapp.todo_app.dto;

public class ValidateResponse {
    private boolean valido;
    private String email;
    private String rol;

    public ValidateResponse() {
    }

    public ValidateResponse(boolean valido, String email, String rol) {
        this.valido = valido;
        this.email = email;
        this.rol = rol;
    }

    public boolean isValido() { return valido; }
    public void setValido(boolean valido) { this.valido = valido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
