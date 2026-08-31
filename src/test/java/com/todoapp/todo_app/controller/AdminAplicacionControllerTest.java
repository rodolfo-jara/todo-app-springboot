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
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void adminDebeAgregarUsuarioExistenteASuAplicacion() throws Exception {

        Aplicacion appA = aplicacionRepository.save(
                new Aplicacion(
                        "admin-agregar-app-a",
                        "Admin Agregar App A"
                )
        );

        Aplicacion appB = aplicacionRepository.save(
                new Aplicacion(
                        "admin-agregar-app-b",
                        "Admin Agregar App B"
                )
        );

        Usuario admin = crearUsuario(
                "Admin Agregar",
                "admin-agregar@test.com"
        );

        Usuario usuario = crearUsuario(
                "Usuario Global Agregar",
                "usuario-global-agregar@test.com"
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        admin,
                        appA,
                        RolAplicacion.ADMIN
                )
        );

        // El usuario ya pertenece a otra app.
        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        usuario,
                        appB,
                        RolAplicacion.USER
                )
        );

        String token = jwtService.generarToken(
                admin.getEmail(),
                "ADMIN",
                "admin-agregar-app-a",
                false
        );

        String json = """
            {
                "email": "USUARIO-GLOBAL-AGREGAR@TEST.COM"
            }
            """;

        mockMvc.perform(
                        post("/api/admin/usuarios")
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-agregar-app-a")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.usuarioId")
                                .value(usuario.getId())
                )
                .andExpect(
                        jsonPath("$.email")
                                .value("usuario-global-agregar@test.com")
                )
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.activo").value(true));

        UsuarioAplicacion nuevoAcceso =
                usuarioAplicacionRepository
                        .findByUsuarioIdAndAplicacionId(
                                usuario.getId(),
                                appA.getId()
                        )
                        .orElseThrow();

        assertEquals("USER", nuevoAcceso.getRol());
        assertTrue(nuevoAcceso.isActivo());

        // Su acceso previo a appB sigue existiendo.
        assertTrue(
                usuarioAplicacionRepository
                        .findByUsuarioIdAndAplicacionId(
                                usuario.getId(),
                                appB.getId()
                        )
                        .isPresent()
        );
    }
    @Test
    void adminNoDebeAgregarUsuarioGlobalInexistente() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-usuario-inexistente",
                        "Admin Usuario Inexistente"
                )
        );

        Usuario admin = crearUsuario(
                "Admin Usuario Inexistente",
                "admin-usuario-inexistente@test.com"
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        admin,
                        aplicacion,
                        RolAplicacion.ADMIN
                )
        );

        String token = jwtService.generarToken(
                admin.getEmail(),
                "ADMIN",
                "admin-usuario-inexistente",
                false
        );

        String json = """
            {
                "email": "no-existe@test.com"
            }
            """;

        mockMvc.perform(
                        post("/api/admin/usuarios")
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-usuario-inexistente")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound());
    }
    @Test
    void adminNoDebeAgregarUsuarioDuplicadoASuAplicacion() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-duplicado-app",
                        "Admin Duplicado App"
                )
        );

        Usuario admin = crearUsuario(
                "Admin Duplicado",
                "admin-duplicado@test.com"
        );

        Usuario usuario = crearUsuario(
                "Usuario Duplicado",
                "usuario-duplicado-admin@test.com"
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        admin,
                        aplicacion,
                        RolAplicacion.ADMIN
                )
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        usuario,
                        aplicacion,
                        RolAplicacion.USER
                )
        );

        String token = jwtService.generarToken(
                admin.getEmail(),
                "ADMIN",
                "admin-duplicado-app",
                false
        );

        String json = """
            {
                "email": "usuario-duplicado-admin@test.com"
            }
            """;

        mockMvc.perform(
                        post("/api/admin/usuarios")
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-duplicado-app")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict());
    }
    @Test
    void adminDebeReactivarUsuarioInactivoComoUser() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-reactivar-app",
                        "Admin Reactivar App"
                )
        );

        Usuario admin = crearUsuario(
                "Admin Reactivar",
                "admin-reactivar@test.com"
        );

        Usuario usuario = crearUsuario(
                "Usuario Reactivar",
                "usuario-reactivar-admin@test.com"
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        admin,
                        aplicacion,
                        RolAplicacion.ADMIN
                )
        );

        UsuarioAplicacion accesoAntiguo =
                new UsuarioAplicacion(
                        usuario,
                        aplicacion,
                        RolAplicacion.ADMIN
                );

        accesoAntiguo.setActivo(false);

        accesoAntiguo =
                usuarioAplicacionRepository.save(accesoAntiguo);

        String token = jwtService.generarToken(
                admin.getEmail(),
                "ADMIN",
                "admin-reactivar-app",
                false
        );

        String json = """
            {
                "email": "usuario-reactivar-admin@test.com"
            }
            """;

        mockMvc.perform(
                        post("/api/admin/usuarios")
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-reactivar-app")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.activo").value(true));

        UsuarioAplicacion actualizado =
                usuarioAplicacionRepository
                        .findById(accesoAntiguo.getId())
                        .orElseThrow();

        assertEquals("USER", actualizado.getRol());
        assertTrue(actualizado.isActivo());
    }
    @Test
    void adminDegradadoNoDebeAgregarUsuarios() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-agregar-degradado",
                        "Admin Agregar Degradado"
                )
        );

        Usuario antiguoAdmin = crearUsuario(
                "Antiguo Admin Agregar",
                "antiguo-admin-agregar@test.com"
        );

        Usuario usuario = crearUsuario(
                "Usuario Para Degradado",
                "usuario-para-degradado@test.com"
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        antiguoAdmin,
                        aplicacion,
                        RolAplicacion.USER
                )
        );

        String token = jwtService.generarToken(
                antiguoAdmin.getEmail(),
                "ADMIN",
                "admin-agregar-degradado",
                false
        );

        String json = """
            {
                "email": "usuario-para-degradado@test.com"
            }
            """;

        mockMvc.perform(
                        post("/api/admin/usuarios")
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-agregar-degradado")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void adminNoDebeAgregarSuperAdminASuAplicacion() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-superadmin-protegido",
                        "Admin SuperAdmin Protegido"
                )
        );

        Usuario admin = crearUsuario(
                "Admin Local Proteccion",
                "admin-local-proteccion@test.com"
        );

        Usuario superAdmin = crearUsuario(
                "Super Admin Global",
                "superadmin-global-protegido@test.com"
        );

        superAdmin.setSuperAdmin(true);
        superAdmin = usuarioRepository.save(superAdmin);

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        admin,
                        aplicacion,
                        RolAplicacion.ADMIN
                )
        );

        String token = jwtService.generarToken(
                admin.getEmail(),
                "ADMIN",
                "admin-superadmin-protegido",
                false
        );

        String json = """
            {
                "email": "superadmin-global-protegido@test.com"
            }
            """;

        mockMvc.perform(
                        post("/api/admin/usuarios")
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-superadmin-protegido")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isForbidden());
    }
}