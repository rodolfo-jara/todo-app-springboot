package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.entity.Aplicacion;
import com.todoapp.todo_app.entity.RolAplicacion;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.entity.UsuarioAplicacion;
import com.todoapp.todo_app.repository.AplicacionRepository;
import com.todoapp.todo_app.repository.UsuarioAplicacionRepository;
import com.todoapp.todo_app.repository.UsuarioRepository;
import com.todoapp.todo_app.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAplicacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    @Autowired
    private UsuarioAplicacionRepository usuarioAplicacionRepository;

    @Test
    void adminDebeListarSoloUsuariosDeSuAplicacion() throws Exception {

        Aplicacion appA = aplicacionRepository.save(
                new Aplicacion(
                        "admin-listado-app-a",
                        "Admin Listado App A"
                )
        );

        Aplicacion appB = aplicacionRepository.save(
                new Aplicacion(
                        "admin-listado-app-b",
                        "Admin Listado App B"
                )
        );

        Usuario admin = crearUsuario(
                "Admin App A",
                "admin-listado-a@test.com"
        );

        Usuario usuarioA = crearUsuario(
                "Usuario App A",
                "usuario-listado-a@test.com"
        );

        Usuario usuarioB = crearUsuario(
                "Usuario App B",
                "usuario-listado-b@test.com"
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        admin,
                        appA,
                        RolAplicacion.ADMIN
                )
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        usuarioA,
                        appA,
                        RolAplicacion.USER
                )
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        usuarioB,
                        appB,
                        RolAplicacion.USER
                )
        );

        String token = jwtService.generarToken(
                admin.getEmail(),
                "ADMIN",
                "admin-listado-app-a",
                false
        );

        mockMvc.perform(
                        get("/api/admin/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "admin-listado-app-a"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[*].email")
                                .value(hasItem("usuario-listado-a@test.com"))
                )
                .andExpect(
                        jsonPath("$[*].email")
                                .value(not(hasItem("usuario-listado-b@test.com")))
                );
    }

    @Test
    void userNoDebeListarUsuariosDeAplicacion() throws Exception {

        String token = jwtService.generarToken(
                "user-admin-endpoint@test.com",
                "USER",
                "todo-app",
                false
        );

        mockMvc.perform(
                        get("/api/admin/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "todo-app"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDegradadoAUserEnBdNoDebeListarUsuarios() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-degradado-test",
                        "Admin Degradado Test"
                )
        );

        Usuario usuario = crearUsuario(
                "Admin Degradado",
                "admin-degradado@test.com"
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        usuario,
                        aplicacion,
                        RolAplicacion.USER
                )
        );

        String token = jwtService.generarToken(
                usuario.getEmail(),
                "ADMIN",
                "admin-degradado-test",
                false
        );

        mockMvc.perform(
                        get("/api/admin/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "admin-degradado-test"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminConAccesoInactivoNoDebeListarUsuarios() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-inactivo-test",
                        "Admin Inactivo Test"
                )
        );

        Usuario usuario = crearUsuario(
                "Admin Inactivo",
                "admin-inactivo@test.com"
        );

        UsuarioAplicacion acceso = new UsuarioAplicacion(
                usuario,
                aplicacion,
                RolAplicacion.ADMIN
        );

        acceso.setActivo(false);

        usuarioAplicacionRepository.save(acceso);

        String token = jwtService.generarToken(
                usuario.getEmail(),
                "ADMIN",
                "admin-inactivo-test",
                false
        );

        mockMvc.perform(
                        get("/api/admin/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "admin-inactivo-test"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminDeAplicacionInactivaNoDebeListarUsuarios() throws Exception {

        Aplicacion aplicacion = new Aplicacion(
                "admin-app-inactiva-test",
                "Admin App Inactiva Test"
        );

        aplicacion.setActivo(false);
        aplicacion = aplicacionRepository.save(aplicacion);

        Usuario usuario = crearUsuario(
                "Admin App Inactiva",
                "admin-app-inactiva@test.com"
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        usuario,
                        aplicacion,
                        RolAplicacion.ADMIN
                )
        );

        String token = jwtService.generarToken(
                usuario.getEmail(),
                "ADMIN",
                "admin-app-inactiva-test",
                false
        );

        mockMvc.perform(
                        get("/api/admin/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "admin-app-inactiva-test"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void adminNoDebeUsarTokenDeUnaAppEnOtraApp() throws Exception {

        String token = jwtService.generarToken(
                "admin-cruzado@test.com",
                "ADMIN",
                "app-a",
                false
        );

        mockMvc.perform(
                        get("/api/admin/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "app-b"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    private Usuario crearUsuario(
            String nombre,
            String email
    ) {
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword("password-test");

        return usuarioRepository.save(usuario);
    }
}