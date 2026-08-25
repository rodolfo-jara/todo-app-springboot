package com.todoapp.todo_app.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "usuario_aplicacion",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_usuario_aplicacion",
                        columnNames = {"usuario_id", "aplicacion_id"}
                )
        }
)
public class UsuarioAplicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "aplicacion_id", nullable = false)
    private Aplicacion aplicacion;

    @Column(nullable = false)
    private String rol;

    @Column(nullable = false)
    private boolean activo = true;

    public UsuarioAplicacion() {
    }

    public UsuarioAplicacion(
            Usuario usuario,
            Aplicacion aplicacion,
            String rol
    ) {
        this.usuario = usuario;
        this.aplicacion = aplicacion;
        this.rol = rol;
        this.activo = true;
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Aplicacion getAplicacion() {
        return aplicacion;
    }

    public void setAplicacion(Aplicacion aplicacion) {
        this.aplicacion = aplicacion;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}