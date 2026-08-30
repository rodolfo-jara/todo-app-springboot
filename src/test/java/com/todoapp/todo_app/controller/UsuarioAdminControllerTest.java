package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.entity.*;
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
import org.springframework.http.MediaType;

import java.time.Instant;
import com.todoapp.todo_app.entity.RefreshToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioAdminControllerTest {

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
    void superAdminDebeListarTodosLosUsuarios() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Global Test");
        usuario.setEmail("usuario-global@test.com");
        usuario.setPassword("password-test");

        usuarioRepository.save(usuario);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        mockMvc.perform(
                        get("/api/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "auth-admin"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[*].email")
                                .value(hasItem("usuario-global@test.com"))
                )
                .andExpect(
                        jsonPath("$[*].nombre")
                                .value(hasItem("Usuario Global Test"))
                )
                .andExpect(
                        jsonPath("$[*].password").doesNotExist()
                );
    }
    @Test
    void adminNormalNoDebeListarTodosLosUsuarios() throws Exception {

        String token = jwtService.generarToken(
                "admin@test.com",
                "ADMIN",
                "todo-app",
                false
        );

        mockMvc.perform(
                        get("/api/usuarios")
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
    void superAdminDebeConsultarUsuarioPorId() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Consulta Test");
        usuario.setEmail("usuario-consulta@test.com");
        usuario.setPassword("password-test");

        Usuario guardado = usuarioRepository.save(usuario);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        mockMvc.perform(
                        get("/api/usuarios/{id}", guardado.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "auth-admin"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId()))
                .andExpect(jsonPath("$.nombre").value("Usuario Consulta Test"))
                .andExpect(jsonPath("$.email").value("usuario-consulta@test.com"))
                .andExpect(jsonPath("$.superAdmin").value(false))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
    @Test
    void consultarUsuarioInexistenteDebeRetornar404() throws Exception {

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        mockMvc.perform(
                        get("/api/usuarios/{id}", Long.MAX_VALUE)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "auth-admin"
                                )
                )
                .andExpect(status().isNotFound());
    }
    @Test
    void adminNormalNoDebeConsultarUsuarioPorId() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Protegido Test");
        usuario.setEmail("usuario-protegido@test.com");
        usuario.setPassword("password-test");

        Usuario guardado = usuarioRepository.save(usuario);

        String token = jwtService.generarToken(
                "admin@test.com",
                "ADMIN",
                "todo-app",
                false
        );

        mockMvc.perform(
                        get("/api/usuarios/{id}", guardado.getId())
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
    void superAdminDebeVerAplicacionesDeUnUsuario() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Apps Test");
        usuario.setEmail("usuario-apps@test.com");
        usuario.setPassword("password-test");

        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion appA = aplicacionRepository.save(
                new Aplicacion(
                        "usuario-app-a",
                        "Usuario App A"
                )
        );

        Aplicacion appB = aplicacionRepository.save(
                new Aplicacion(
                        "usuario-app-b",
                        "Usuario App B"
                )
        );

        UsuarioAplicacion accesoA = new UsuarioAplicacion(
                guardado,
                appA,
                RolAplicacion.USER
        );

        UsuarioAplicacion accesoB = new UsuarioAplicacion(
                guardado,
                appB,
                RolAplicacion.ADMIN
        );

        accesoB.setActivo(false);

        usuarioAplicacionRepository.save(accesoA);
        usuarioAplicacionRepository.save(accesoB);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        mockMvc.perform(
                        get("/api/usuarios/{id}/aplicaciones", guardado.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "auth-admin"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[*].codigo")
                                .value(hasItem("usuario-app-a"))
                )
                .andExpect(
                        jsonPath("$[*].codigo")
                                .value(hasItem("usuario-app-b"))
                )
                .andExpect(
                        jsonPath("$[?(@.codigo == 'usuario-app-a')].rol")
                                .value(hasItem("USER"))
                )
                .andExpect(
                        jsonPath("$[?(@.codigo == 'usuario-app-b')].rol")
                                .value(hasItem("ADMIN"))
                )
                .andExpect(
                        jsonPath("$[?(@.codigo == 'usuario-app-a')].activo")
                                .value(hasItem(true))
                )
                .andExpect(
                        jsonPath("$[?(@.codigo == 'usuario-app-b')].activo")
                                .value(hasItem(false))
                );
    }

    @Test
    void usuarioSinAplicacionesDebeRetornarListaVacia() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Sin Apps");
        usuario.setEmail("usuario-sin-apps@test.com");
        usuario.setPassword("password-test");

        Usuario guardado = usuarioRepository.save(usuario);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        mockMvc.perform(
                        get("/api/usuarios/{id}/aplicaciones", guardado.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "auth-admin"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void aplicacionesDeUsuarioInexistenteDebeRetornar404() throws Exception {

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        mockMvc.perform(
                        get("/api/usuarios/{id}/aplicaciones", Long.MAX_VALUE)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "auth-admin"
                                )
                )
                .andExpect(status().isNotFound());
    }
    @Test
    void adminNormalNoDebeVerAplicacionesDeOtroUsuario() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Protegido Apps");
        usuario.setEmail("usuario-protegido-apps@test.com");
        usuario.setPassword("password-test");

        Usuario guardado = usuarioRepository.save(usuario);

        String token = jwtService.generarToken(
                "admin@test.com",
                "ADMIN",
                "todo-app",
                false
        );

        mockMvc.perform(
                        get("/api/usuarios/{id}/aplicaciones", guardado.getId())
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
    void superAdminDebeAsignarUsuarioAUnaAplicacion() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Asignacion");
        usuario.setEmail("usuario-asignacion@test.com");
        usuario.setPassword("password-test");
        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "app-asignacion-test",
                        "App Asignacion Test"
                )
        );

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "aplicacionId": %d
            }
            """.formatted(aplicacion.getId());

        mockMvc.perform(
                        post("/api/usuarios/{id}/aplicaciones", guardado.getId())
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aplicacionId").value(aplicacion.getId()))
                .andExpect(jsonPath("$.codigo").value("app-asignacion-test"))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.activo").value(true));
    }
    @Test
    void asignarAplicacionDuplicadaDebeRetornar409() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Duplicado App");
        usuario.setEmail("usuario-duplicado-app@test.com");
        usuario.setPassword("password-test");
        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "app-duplicada-usuario-test",
                        "App Duplicada Usuario Test"
                )
        );

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "aplicacionId": %d
            }
            """.formatted(aplicacion.getId());

        mockMvc.perform(
                        post("/api/usuarios/{id}/aplicaciones", guardado.getId())
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/usuarios/{id}/aplicaciones", guardado.getId())
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void asignarAplicacionInactivaDebeRetornar409() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario App Inactiva");
        usuario.setEmail("usuario-app-inactiva@test.com");
        usuario.setPassword("password-test");
        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion aplicacion = new Aplicacion(
                "app-inactiva-asignacion-test",
                "App Inactiva Asignacion Test"
        );

        aplicacion.setActivo(false);
        aplicacion = aplicacionRepository.save(aplicacion);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "aplicacionId": %d
            }
            """.formatted(aplicacion.getId());

        mockMvc.perform(
                        post("/api/usuarios/{id}/aplicaciones", guardado.getId())
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void asignarAplicacionAUsuarioInexistenteDebeRetornar404() throws Exception {

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "app-usuario-inexistente-test",
                        "App Usuario Inexistente Test"
                )
        );

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "aplicacionId": %d
            }
            """.formatted(aplicacion.getId());

        mockMvc.perform(
                        post("/api/usuarios/{id}/aplicaciones", Long.MAX_VALUE)
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound());
    }
    @Test
    void asignarAplicacionInexistenteDebeRetornar404() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario App No Existe");
        usuario.setEmail("usuario-app-no-existe@test.com");
        usuario.setPassword("password-test");
        Usuario guardado = usuarioRepository.save(usuario);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "aplicacionId": %d
            }
            """.formatted(Long.MAX_VALUE);

        mockMvc.perform(
                        post("/api/usuarios/{id}/aplicaciones", guardado.getId())
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound());
    }
    @Test
    void adminNormalNoDebeAsignarUsuarioAUnaAplicacion() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Protegido Asignacion");
        usuario.setEmail("usuario-protegido-asignacion@test.com");
        usuario.setPassword("password-test");
        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "app-protegida-asignacion-test",
                        "App Protegida Asignacion Test"
                )
        );

        String token = jwtService.generarToken(
                "admin@test.com",
                "ADMIN",
                "todo-app",
                false
        );

        String json = """
            {
                "aplicacionId": %d
            }
            """.formatted(aplicacion.getId());

        mockMvc.perform(
                        post("/api/usuarios/{id}/aplicaciones", guardado.getId())
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "todo-app")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void asignarAplicacionDebeReactivarAccesoInactivoComoUser() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Reactivacion");
        usuario.setEmail("usuario-reactivacion@test.com");
        usuario.setPassword("password-test");

        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "app-reactivacion-test",
                        "App Reactivacion Test"
                )
        );

        UsuarioAplicacion acceso = new UsuarioAplicacion(
                guardado,
                aplicacion,
                RolAplicacion.ADMIN
        );

        acceso.setActivo(false);

        usuarioAplicacionRepository.save(acceso);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "aplicacionId": %d
            }
            """.formatted(aplicacion.getId());

        mockMvc.perform(
                        post("/api/usuarios/{id}/aplicaciones", guardado.getId())
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aplicacionId").value(aplicacion.getId()))
                .andExpect(jsonPath("$.rol").value("USER"))
                .andExpect(jsonPath("$.activo").value(true));
    }
    @Test
    void superAdminDebeQuitarAplicacionYRevocarSoloSusRefreshTokens() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Quitar App");
        usuario.setEmail("usuario-quitar-app@test.com");
        usuario.setPassword("password-test");

        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion appA = aplicacionRepository.save(
                new Aplicacion(
                        "quitar-app-a",
                        "Quitar App A"
                )
        );

        Aplicacion appB = aplicacionRepository.save(
                new Aplicacion(
                        "quitar-app-b",
                        "Quitar App B"
                )
        );

        UsuarioAplicacion accesoA = new UsuarioAplicacion(
                guardado,
                appA,
                RolAplicacion.USER
        );

        UsuarioAplicacion accesoB = new UsuarioAplicacion(
                guardado,
                appB,
                RolAplicacion.USER
        );

        accesoA = usuarioAplicacionRepository.save(accesoA);
        usuarioAplicacionRepository.save(accesoB);

        RefreshToken tokenA1 = new RefreshToken();
        tokenA1.setTokenHash("hash-quitar-app-a-1");
        tokenA1.setUsuario(guardado);
        tokenA1.setAplicacion(appA);
        tokenA1.setCreadoEn(Instant.now());
        tokenA1.setExpiraEn(Instant.now().plusSeconds(3600));
        tokenA1.setRevocado(false);

        RefreshToken tokenA2 = new RefreshToken();
        tokenA2.setTokenHash("hash-quitar-app-a-2");
        tokenA2.setUsuario(guardado);
        tokenA2.setAplicacion(appA);
        tokenA2.setCreadoEn(Instant.now());
        tokenA2.setExpiraEn(Instant.now().plusSeconds(3600));
        tokenA2.setRevocado(false);

        RefreshToken tokenB = new RefreshToken();
        tokenB.setTokenHash("hash-quitar-app-b");
        tokenB.setUsuario(guardado);
        tokenB.setAplicacion(appB);
        tokenB.setCreadoEn(Instant.now());
        tokenB.setExpiraEn(Instant.now().plusSeconds(3600));
        tokenB.setRevocado(false);

        tokenA1 = refreshTokenRepository.save(tokenA1);
        tokenA2 = refreshTokenRepository.save(tokenA2);
        tokenB = refreshTokenRepository.save(tokenB);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        mockMvc.perform(
                        delete(
                                "/api/usuarios/{usuarioId}/aplicaciones/{aplicacionId}",
                                guardado.getId(),
                                appA.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.aplicacionId").value(appA.getId()))
                .andExpect(jsonPath("$.codigo").value("quitar-app-a"))
                .andExpect(jsonPath("$.activo").value(false));

        UsuarioAplicacion accesoActualizado =
                usuarioAplicacionRepository
                        .findByUsuarioIdAndAplicacionId(
                                guardado.getId(),
                                appA.getId()
                        )
                        .orElseThrow();

        assertFalse(accesoActualizado.isActivo());

        RefreshToken tokenA1Actualizado =
                refreshTokenRepository.findById(tokenA1.getId())
                        .orElseThrow();

        RefreshToken tokenA2Actualizado =
                refreshTokenRepository.findById(tokenA2.getId())
                        .orElseThrow();

        RefreshToken tokenBActualizado =
                refreshTokenRepository.findById(tokenB.getId())
                        .orElseThrow();

        assertTrue(tokenA1Actualizado.isRevocado());
        assertTrue(tokenA2Actualizado.isRevocado());

        assertFalse(tokenBActualizado.isRevocado());
    }
    @Test
    void adminNormalNoDebeQuitarUsuarioDeUnaAplicacion() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Quitar Protegido");
        usuario.setEmail("usuario-quitar-protegido@test.com");
        usuario.setPassword("password-test");

        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "app-quitar-protegida",
                        "App Quitar Protegida"
                )
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        guardado,
                        aplicacion,
                        RolAplicacion.USER
                )
        );

        String token = jwtService.generarToken(
                "admin@test.com",
                "ADMIN",
                "todo-app",
                false
        );

        mockMvc.perform(
                        delete(
                                "/api/usuarios/{usuarioId}/aplicaciones/{aplicacionId}",
                                guardado.getId(),
                                aplicacion.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "todo-app")
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void noDebeQuitarAuthAdminAUnSuperAdmin() throws Exception {

        Usuario superAdmin = new Usuario();
        superAdmin.setNombre("Super Admin Protegido");
        superAdmin.setEmail("superadmin-protegido@test.com");
        superAdmin.setPassword("password-test");
        superAdmin.setSuperAdmin(true);

        Usuario guardado = usuarioRepository.save(superAdmin);

        Aplicacion authAdmin = aplicacionRepository
                .findByCodigo("auth-admin")
                .orElseGet(() ->
                        aplicacionRepository.save(
                                new Aplicacion(
                                        "auth-admin",
                                        "Auth Admin"
                                )
                        )
                );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        guardado,
                        authAdmin,
                        RolAplicacion.ADMIN
                )
        );

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        mockMvc.perform(
                        delete(
                                "/api/usuarios/{usuarioId}/aplicaciones/{aplicacionId}",
                                guardado.getId(),
                                authAdmin.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                )
                .andExpect(status().isConflict());

        UsuarioAplicacion acceso =
                usuarioAplicacionRepository
                        .findByUsuarioIdAndAplicacionId(
                                guardado.getId(),
                                authAdmin.getId()
                        )
                        .orElseThrow();

        assertTrue(acceso.isActivo());
    }

    @Test
    void superAdminDebeCambiarRolUserAAdmin() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Cambio Rol");
        usuario.setEmail("usuario-cambio-rol@test.com");
        usuario.setPassword("password-test");
        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "app-cambio-rol-test",
                        "App Cambio Rol Test"
                )
        );

        UsuarioAplicacion acceso =
                usuarioAplicacionRepository.save(
                        new UsuarioAplicacion(
                                guardado,
                                aplicacion,
                                RolAplicacion.USER
                        )
                );

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "rol": "ADMIN"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/usuarios/{usuarioId}/aplicaciones/{aplicacionId}/rol",
                                guardado.getId(),
                                aplicacion.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.activo").value(true));

        UsuarioAplicacion actualizado =
                usuarioAplicacionRepository.findById(acceso.getId())
                        .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                "ADMIN",
                actualizado.getRol()
        );
    }
    @Test
    void superAdminDebeCambiarRolAdminAUser() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Admin A User");
        usuario.setEmail("usuario-admin-user@test.com");
        usuario.setPassword("password-test");
        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "app-admin-user-test",
                        "App Admin User Test"
                )
        );

        UsuarioAplicacion acceso =
                usuarioAplicacionRepository.save(
                        new UsuarioAplicacion(
                                guardado,
                                aplicacion,
                                RolAplicacion.ADMIN
                        )
                );

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "rol": "USER"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/usuarios/{usuarioId}/aplicaciones/{aplicacionId}/rol",
                                guardado.getId(),
                                aplicacion.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("USER"));

        UsuarioAplicacion actualizado =
                usuarioAplicacionRepository.findById(acceso.getId())
                        .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                "USER",
                actualizado.getRol()
        );
    }
    @Test
    void cambiarRolDeAccesoInactivoDebeRetornar409() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Rol Inactivo");
        usuario.setEmail("usuario-rol-inactivo@test.com");
        usuario.setPassword("password-test");
        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "app-rol-inactivo-test",
                        "App Rol Inactivo Test"
                )
        );

        UsuarioAplicacion acceso = new UsuarioAplicacion(
                guardado,
                aplicacion,
                RolAplicacion.USER
        );

        acceso.setActivo(false);
        usuarioAplicacionRepository.save(acceso);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "rol": "ADMIN"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/usuarios/{usuarioId}/aplicaciones/{aplicacionId}/rol",
                                guardado.getId(),
                                aplicacion.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict());
    }
    @Test
    void cambiarRolSinAccesoDebeRetornar404() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Sin Acceso Rol");
        usuario.setEmail("usuario-sin-acceso-rol@test.com");
        usuario.setPassword("password-test");
        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "app-sin-acceso-rol-test",
                        "App Sin Acceso Rol Test"
                )
        );

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "rol": "ADMIN"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/usuarios/{usuarioId}/aplicaciones/{aplicacionId}/rol",
                                guardado.getId(),
                                aplicacion.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound());
    }
    @Test
    void adminNormalNoDebeCambiarRolGlobalmente() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Rol Protegido");
        usuario.setEmail("usuario-rol-protegido@test.com");
        usuario.setPassword("password-test");
        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "app-rol-protegida-test",
                        "App Rol Protegida Test"
                )
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        guardado,
                        aplicacion,
                        RolAplicacion.USER
                )
        );

        String token = jwtService.generarToken(
                "admin@test.com",
                "ADMIN",
                "todo-app",
                false
        );

        String json = """
            {
                "rol": "ADMIN"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/usuarios/{usuarioId}/aplicaciones/{aplicacionId}/rol",
                                guardado.getId(),
                                aplicacion.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "todo-app")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isForbidden());
    }
    @Test
    void noDebeCambiarAuthAdminDeSuperAdminAUser() throws Exception {

        Usuario superAdmin = new Usuario();
        superAdmin.setNombre("Super Admin Rol Protegido");
        superAdmin.setEmail("superadmin-rol-protegido@test.com");
        superAdmin.setPassword("password-test");
        superAdmin.setSuperAdmin(true);

        Usuario guardado = usuarioRepository.save(superAdmin);

        Aplicacion authAdmin = aplicacionRepository
                .findByCodigo("auth-admin")
                .orElseGet(() ->
                        aplicacionRepository.save(
                                new Aplicacion(
                                        "auth-admin",
                                        "Auth Admin"
                                )
                        )
                );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        guardado,
                        authAdmin,
                        RolAplicacion.ADMIN
                )
        );

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "rol": "USER"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/usuarios/{usuarioId}/aplicaciones/{aplicacionId}/rol",
                                guardado.getId(),
                                authAdmin.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict());
    }
    @Test
    void cambiarRolASuperAdminDebeRetornar400() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Intento SuperAdmin");
        usuario.setEmail("usuario-intento-superadmin@test.com");
        usuario.setPassword("password-test");
        Usuario guardado = usuarioRepository.save(usuario);

        Aplicacion aplicacion = aplicacionRepository.save(
                new Aplicacion(
                        "app-intento-superadmin-test",
                        "App Intento SuperAdmin Test"
                )
        );

        usuarioAplicacionRepository.save(
                new UsuarioAplicacion(
                        guardado,
                        aplicacion,
                        RolAplicacion.USER
                )
        );

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "rol": "SUPER_ADMIN"
            }
            """;

        mockMvc.perform(
                        patch(
                                "/api/usuarios/{usuarioId}/aplicaciones/{aplicacionId}/rol",
                                guardado.getId(),
                                aplicacion.getId()
                        )
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest());
    }

}