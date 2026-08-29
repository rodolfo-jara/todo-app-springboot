package com.todoapp.todo_app.bootstrap;

import com.todoapp.todo_app.entity.Aplicacion;
import com.todoapp.todo_app.entity.RolAplicacion;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.entity.UsuarioAplicacion;
import com.todoapp.todo_app.repository.AplicacionRepository;
import com.todoapp.todo_app.repository.UsuarioAplicacionRepository;
import com.todoapp.todo_app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
public class SuperAdminInitializer implements CommandLineRunner {

    private static final String ADMIN_APP_CODE = "auth-admin";
    private static final String ADMIN_APP_NAME = "Authentication Admin";

    private final UsuarioRepository usuarioRepository;
    private final AplicacionRepository aplicacionRepository;
    private final UsuarioAplicacionRepository usuarioAplicacionRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${SUPER_ADMIN_EMAIL:}")
    private String superAdminEmail;

    @Value("${SUPER_ADMIN_PASSWORD:}")
    private String superAdminPassword;

    @Value("${SUPER_ADMIN_NAME:Super Admin}")
    private String superAdminName;

    public SuperAdminInitializer(
            UsuarioRepository usuarioRepository,
            AplicacionRepository aplicacionRepository,
            UsuarioAplicacionRepository usuarioAplicacionRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.aplicacionRepository = aplicacionRepository;
        this.usuarioAplicacionRepository = usuarioAplicacionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        if (superAdminEmail == null
                || superAdminEmail.isBlank()
                || superAdminPassword == null
                || superAdminPassword.isBlank()) {
            return;
        }

        String emailNormalizado = superAdminEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        // 1. Crear la aplicación interna de administración si no existe
        Aplicacion adminApp = aplicacionRepository
                .findByCodigo(ADMIN_APP_CODE)
                .orElseGet(() -> aplicacionRepository.save(
                        new Aplicacion(
                                ADMIN_APP_CODE,
                                ADMIN_APP_NAME
                        )
                ));

        // 2. Buscar o crear el usuario SUPER_ADMIN
        Usuario usuario = usuarioRepository
                .findByEmail(emailNormalizado)
                .orElseGet(() -> {

                    Usuario nuevo = new Usuario();

                    nuevo.setNombre(superAdminName);
                    nuevo.setEmail(emailNormalizado);
                    nuevo.setPassword(
                            passwordEncoder.encode(superAdminPassword)
                    );
                    nuevo.setSuperAdmin(true);

                    return usuarioRepository.save(nuevo);
                });

        // 3. Si ya existía, asegurar que sea SUPER_ADMIN
        if (!usuario.isSuperAdmin()) {
            usuario.setSuperAdmin(true);
            usuarioRepository.save(usuario);
        }

        // 4. Darle acceso a la aplicación interna de administración
        if (usuarioAplicacionRepository
                .findByUsuarioEmailAndAplicacionCodigo(
                        emailNormalizado,
                        ADMIN_APP_CODE
                )
                .isEmpty()) {

            UsuarioAplicacion acceso = new UsuarioAplicacion(
                    usuario,
                    adminApp,
                    RolAplicacion.ADMIN
            );

            usuarioAplicacionRepository.save(acceso);
        }
    }
}