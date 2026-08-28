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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolAplicacion rol;

    @Column(nullable = false)
    private boolean activo = true;

    public UsuarioAplicacion() {
    }


    public UsuarioAplicacion(
            Usuario usuario,
            Aplicacion aplicacion,
            RolAplicacion rol
    ) {
        this.usuario = usuario;
        this.aplicacion = aplicacion;
        this.rol = rol;
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
        return rol.name();
    }

    public void setRol(String rol) {
        this.rol = RolAplicacion.valueOf(rol);
    }
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}