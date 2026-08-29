package com.todoapp.todo_app.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String rol;

    @Column(
            name = "super_admin",
            nullable = false,
            columnDefinition = "boolean default false"
    )
    private boolean superAdmin = false;

    // Modelo antiguo: lo eliminaremos más adelante
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "usuario_aplicaciones",
            joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Column(name = "aplicacion")
    private Set<String> aplicaciones = new HashSet<>();

    public Usuario() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }

    public Set<String> getAplicaciones() {
        return aplicaciones;
    }

    public void setAplicaciones(Set<String> aplicaciones) {
        this.aplicaciones = aplicaciones;
    }

    public boolean tieneAcceso(String app) {
        return aplicaciones != null && aplicaciones.contains(app);
    }
}