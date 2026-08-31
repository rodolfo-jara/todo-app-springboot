package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.entity.Aplicacion;
import com.todoapp.todo_app.entity.RolAplicacion;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.entity.UsuarioAplicacion;
import com.todoapp.todo_app.repository.AplicacionRepository;
import com.todoapp.todo_app.repository.RefreshTokenRepository;
import com.todoapp.todo_app.repository.UsuarioAplicacionRepository;
import com.todoapp.todo_app.repository.UsuarioRepository;
import com.todoapp.todo_app.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.todoapp.todo_app.entity.RefreshToken;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;


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
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

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
    @Test
    void adminDebeQuitarUsuarioDeSuAplicacionYRevocarSusRefreshTokens()
            throws Exception {

        Aplicacion appA = aplicacionRepository.save(
                new Aplicacion(
                        "admin-quitar-app-a",
                        "Admin Quitar App A"
                )
        );

        Aplicacion appB = aplicacionRepository.save(
                new Aplicacion(
                        "admin-quitar-app-b",
                        "Admin Quitar App B"
                )
        );

        Usuario admin = crearUsuario(
                "Admin Quitar",
                "admin-quitar@test.com"
        );

        Usuario usuario = crearUsuario(
                "Usuario Quitar",
                "usuario-quitar-admin@test.com"
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
                        usuario,
                        appA,
                        RolAplicacion.USER
                )
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        usuario,
                        appB,
                        RolAplicacion.USER
                )
        );

        RefreshToken tokenA = new RefreshToken();
        tokenA.setTokenHash("hash-admin-quitar-app-a");
        tokenA.setUsuario(usuario);
        tokenA.setAplicacion(appA);
        tokenA.setCreadoEn(Instant.now());
        tokenA.setExpiraEn(Instant.now().plusSeconds(3600));
        tokenA.setRevocado(false);
        tokenA = refreshTokenRepository.save(tokenA);

        RefreshToken tokenB = new RefreshToken();
        tokenB.setTokenHash("hash-admin-quitar-app-b");
        tokenB.setUsuario(usuario);
        tokenB.setAplicacion(appB);
        tokenB.setCreadoEn(Instant.now());
        tokenB.setExpiraEn(Instant.now().plusSeconds(3600));
        tokenB.setRevocado(false);
        tokenB = refreshTokenRepository.save(tokenB);

        String token = jwtService.generarToken(
                admin.getEmail(),
                "ADMIN",
                "admin-quitar-app-a",
                false
        );

        mockMvc.perform(
                        delete(
                                "/api/admin/usuarios/{usuarioId}",
                                usuario.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-quitar-app-a")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuario.getId()))
                .andExpect(jsonPath("$.activo").value(false));

        UsuarioAplicacion accesoA =
                usuarioAplicacionRepository
                        .findByUsuarioIdAndAplicacionId(
                                usuario.getId(),
                                appA.getId()
                        )
                        .orElseThrow();

        UsuarioAplicacion accesoB =
                usuarioAplicacionRepository
                        .findByUsuarioIdAndAplicacionId(
                                usuario.getId(),
                                appB.getId()
                        )
                        .orElseThrow();

        assertFalse(accesoA.isActivo());
        assertTrue(accesoB.isActivo());

        RefreshToken tokenAActualizado =
                refreshTokenRepository.findById(tokenA.getId())
                        .orElseThrow();

        RefreshToken tokenBActualizado =
                refreshTokenRepository.findById(tokenB.getId())
                        .orElseThrow();

        assertTrue(tokenAActualizado.isRevocado());
        assertFalse(tokenBActualizado.isRevocado());
    }

    @Test
    void adminNoDebeQuitarUsuarioDeOtraAplicacion() throws Exception {

        Aplicacion appA = aplicacionRepository.save(
                new Aplicacion(
                        "admin-quitar-aislamiento-a",
                        "Admin Quitar Aislamiento A"
                )
        );

        Aplicacion appB = aplicacionRepository.save(
                new Aplicacion(
                        "admin-quitar-aislamiento-b",
                        "Admin Quitar Aislamiento B"
                )
        );

        Usuario admin = crearUsuario(
                "Admin Aislamiento",
                "admin-quitar-aislamiento@test.com"
        );

        Usuario usuarioB = crearUsuario(
                "Usuario Solo App B",
                "usuario-solo-app-b@test.com"
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
                        usuarioB,
                        appB,
                        RolAplicacion.USER
                )
        );

        String token = jwtService.generarToken(
                admin.getEmail(),
                "ADMIN",
                "admin-quitar-aislamiento-a",
                false
        );

        mockMvc.perform(
                        delete(
                                "/api/admin/usuarios/{usuarioId}",
                                usuarioB.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-quitar-aislamiento-a")
                )
                .andExpect(status().isNotFound());
    }
    @Test
    void adminDegradadoNoDebeQuitarUsuarios() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-quitar-degradado",
                        "Admin Quitar Degradado"
                )
        );

        Usuario antiguoAdmin = crearUsuario(
                "Admin Degradado Quitar",
                "admin-degradado-quitar@test.com"
        );

        Usuario usuario = crearUsuario(
                "Usuario Degradado Quitar",
                "usuario-degradado-quitar@test.com"
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        antiguoAdmin,
                        aplicacion,
                        RolAplicacion.USER
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
                antiguoAdmin.getEmail(),
                "ADMIN",
                "admin-quitar-degradado",
                false
        );

        mockMvc.perform(
                        delete(
                                "/api/admin/usuarios/{usuarioId}",
                                usuario.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-quitar-degradado")
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void adminNoDebeQuitarSuperAdminDeSuAplicacion() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-quitar-superadmin",
                        "Admin Quitar SuperAdmin"
                )
        );

        Usuario admin = crearUsuario(
                "Admin Local Quitar",
                "admin-local-quitar@test.com"
        );

        Usuario superAdmin = crearUsuario(
                "Super Admin Protegido",
                "superadmin-quitar-protegido@test.com"
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

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        superAdmin,
                        aplicacion,
                        RolAplicacion.USER
                )
        );

        String token = jwtService.generarToken(
                admin.getEmail(),
                "ADMIN",
                "admin-quitar-superadmin",
                false
        );

        mockMvc.perform(
                        delete(
                                "/api/admin/usuarios/{usuarioId}",
                                superAdmin.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-quitar-superadmin")
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void adminNoDebeQuitarUsuariosUsandoOtraAplicacion() throws Exception {

        String token = jwtService.generarToken(
                "admin-cruzado-quitar@test.com",
                "ADMIN",
                "app-a",
                false
        );

        mockMvc.perform(
                        delete("/api/admin/usuarios/{usuarioId}", 1L)
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "app-b")
                )
                .andExpect(status().isUnauthorized());
    }
    @Test
    void adminDebeCambiarUserAAdminEnSuAplicacion() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-cambiar-user-admin",
                        "Admin Cambiar User Admin"
                )
        );

        Usuario admin = crearUsuario(
                "Admin Cambio Rol",
                "admin-cambio-user-admin@test.com"
        );

        Usuario usuario = crearUsuario(
                "Usuario Promovido",
                "usuario-promovido@test.com"
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        admin,
                        aplicacion,
                        RolAplicacion.ADMIN
                )
        );

        UsuarioAplicacion accesoUsuario =
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
                "admin-cambiar-user-admin",
                false
        );

        String json = """
            {
                "rol": "ADMIN"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/admin/usuarios/{usuarioId}/rol",
                                usuario.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-cambiar-user-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuarioId").value(usuario.getId()))
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.activo").value(true));

        UsuarioAplicacion actualizado =
                usuarioAplicacionRepository
                        .findById(accesoUsuario.getId())
                        .orElseThrow();

        assertEquals("ADMIN", actualizado.getRol());
    }
    @Test
    void adminNoDebeCambiarRolDeUsuarioDeOtraAplicacion() throws Exception {

        Aplicacion appA = aplicacionRepository.save(
                new Aplicacion(
                        "admin-rol-aislamiento-a",
                        "Admin Rol Aislamiento A"
                )
        );

        Aplicacion appB = aplicacionRepository.save(
                new Aplicacion(
                        "admin-rol-aislamiento-b",
                        "Admin Rol Aislamiento B"
                )
        );

        Usuario admin = crearUsuario(
                "Admin App A Rol",
                "admin-app-a-rol@test.com"
        );

        Usuario usuarioB = crearUsuario(
                "Usuario App B Rol",
                "usuario-app-b-rol@test.com"
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
                        usuarioB,
                        appB,
                        RolAplicacion.USER
                )
        );

        String token = jwtService.generarToken(
                admin.getEmail(),
                "ADMIN",
                "admin-rol-aislamiento-a",
                false
        );

        String json = """
            {
                "rol": "ADMIN"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/admin/usuarios/{usuarioId}/rol",
                                usuarioB.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-rol-aislamiento-a")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound());
    }
    @Test
    void adminNoDebeCambiarRolDeAccesoInactivo() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-rol-inactivo",
                        "Admin Rol Inactivo"
                )
        );

        Usuario admin = crearUsuario(
                "Admin Rol Inactivo",
                "admin-rol-inactivo@test.com"
        );

        Usuario usuario = crearUsuario(
                "Usuario Rol Inactivo",
                "usuario-rol-inactivo-app@test.com"
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        admin,
                        aplicacion,
                        RolAplicacion.ADMIN
                )
        );

        UsuarioAplicacion acceso = new UsuarioAplicacion(
                usuario,
                aplicacion,
                RolAplicacion.USER
        );

        acceso.setActivo(false);
        usuarioAplicacionRepository.save(acceso);

        String token = jwtService.generarToken(
                admin.getEmail(),
                "ADMIN",
                "admin-rol-inactivo",
                false
        );

        String json = """
            {
                "rol": "ADMIN"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/admin/usuarios/{usuarioId}/rol",
                                usuario.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-rol-inactivo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict());
    }
    @Test
    void adminDegradadoNoDebeCambiarRoles() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-rol-degradado",
                        "Admin Rol Degradado"
                )
        );

        Usuario antiguoAdmin = crearUsuario(
                "Antiguo Admin Rol",
                "antiguo-admin-rol@test.com"
        );

        Usuario usuario = crearUsuario(
                "Usuario Objetivo Rol",
                "usuario-objetivo-rol@test.com"
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        antiguoAdmin,
                        aplicacion,
                        RolAplicacion.USER
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
                antiguoAdmin.getEmail(),
                "ADMIN",
                "admin-rol-degradado",
                false
        );

        String json = """
            {
                "rol": "ADMIN"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/admin/usuarios/{usuarioId}/rol",
                                usuario.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-rol-degradado")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void adminNoDebeCambiarRolDeSuperAdmin() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-rol-superadmin",
                        "Admin Rol SuperAdmin"
                )
        );

        Usuario admin = crearUsuario(
                "Admin Local Rol",
                "admin-local-rol@test.com"
        );

        Usuario superAdmin = crearUsuario(
                "Super Admin Objetivo",
                "superadmin-objetivo-rol@test.com"
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

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        superAdmin,
                        aplicacion,
                        RolAplicacion.USER
                )
        );

        String token = jwtService.generarToken(
                admin.getEmail(),
                "ADMIN",
                "admin-rol-superadmin",
                false
        );

        String json = """
            {
                "rol": "ADMIN"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/admin/usuarios/{usuarioId}/rol",
                                superAdmin.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-rol-superadmin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void adminDebeCambiarAdminAUserSiExisteOtroAdminActivo() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "admin-degradar-con-respaldo",
                        "Admin Degradar Con Respaldo"
                )
        );

        Usuario adminEjecutor = crearUsuario(
                "Admin Ejecutor",
                "admin-ejecutor-degradar@test.com"
        );

        Usuario adminObjetivo = crearUsuario(
                "Admin Objetivo",
                "admin-objetivo-degradar@test.com"
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        adminEjecutor,
                        aplicacion,
                        RolAplicacion.ADMIN
                )
        );

        UsuarioAplicacion accesoObjetivo =
                usuarioAplicacionRepository.save(
                        new UsuarioAplicacion(
                                adminObjetivo,
                                aplicacion,
                                RolAplicacion.ADMIN
                        )
                );

        String token = jwtService.generarToken(
                adminEjecutor.getEmail(),
                "ADMIN",
                "admin-degradar-con-respaldo",
                false
        );

        String json = """
            {
                "rol": "USER"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/admin/usuarios/{usuarioId}/rol",
                                adminObjetivo.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "admin-degradar-con-respaldo")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.activo").value(true));

        UsuarioAplicacion actualizado =
                usuarioAplicacionRepository
                        .findById(accesoObjetivo.getId())
                        .orElseThrow();

        assertEquals("USER", actualizado.getRol());
    }
    @Test
    void noDebeDegradarAlUltimoAdminActivo() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "ultimo-admin-test",
                        "Ultimo Admin Test"
                )
        );

        Usuario admin = crearUsuario(
                "Ultimo Admin",
                "ultimo-admin@test.com"
        );

        UsuarioAplicacion accesoAdmin =
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
                "ultimo-admin-test",
                false
        );

        String json = """
            {
                "rol": "USER"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/admin/usuarios/{usuarioId}/rol",
                                admin.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "ultimo-admin-test")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict());

        UsuarioAplicacion actualizado =
                usuarioAplicacionRepository
                        .findById(accesoAdmin.getId())
                        .orElseThrow();

        assertEquals("ADMIN", actualizado.getRol());
    }
}