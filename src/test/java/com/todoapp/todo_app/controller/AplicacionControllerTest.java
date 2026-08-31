package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.entity.Aplicacion;
import com.todoapp.todo_app.entity.RefreshToken;
import com.todoapp.todo_app.repository.AplicacionRepository;
import com.todoapp.todo_app.repository.RefreshTokenRepository;
import com.todoapp.todo_app.repository.UsuarioRepository;
import com.todoapp.todo_app.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import com.todoapp.todo_app.entity.RefreshToken;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.entity.RolAplicacion;
import com.todoapp.todo_app.entity.UsuarioAplicacion;
import com.todoapp.todo_app.repository.UsuarioAplicacionRepository;
import org.junit.jupiter.api.BeforeEach;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AplicacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AplicacionRepository aplicacionRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private UsuarioAplicacionRepository usuarioAplicacionRepository;
    @BeforeEach
    void prepararSuperAdmin() {

        Aplicacion authAdmin =
                aplicacionRepository
                        .findByCodigo("auth-admin")
                        .orElse(null);

        if (authAdmin == null) {
            authAdmin = new Aplicacion(
                    "auth-admin",
                    "Auth Admin"
            );
        }

        authAdmin.setActivo(true);
        authAdmin = aplicacionRepository.save(authAdmin);

        Usuario superAdmin =
                usuarioRepository
                        .findByEmail("superadmin@test.com")
                        .orElse(null);

        if (superAdmin == null) {
            superAdmin = new Usuario();
            superAdmin.setNombre("Super Admin Test");
            superAdmin.setEmail("superadmin@test.com");
            superAdmin.setPassword("password-test");
        }

        superAdmin.setSuperAdmin(true);
        superAdmin = usuarioRepository.save(superAdmin);

        UsuarioAplicacion acceso =
                usuarioAplicacionRepository
                        .findByUsuarioIdAndAplicacionId(
                                superAdmin.getId(),
                                authAdmin.getId()
                        )
                        .orElse(null);

        if (acceso == null) {
            acceso = new UsuarioAplicacion(
                    superAdmin,
                    authAdmin,
                    RolAplicacion.ADMIN
            );
        } else {
            acceso.setRol("ADMIN");
        }

        acceso.setActivo(true);

        usuarioAplicacionRepository.save(acceso);
    }
    @Test
    void crearAplicacionDebeNormalizarCodigo() throws Exception {

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
                {
                    "codigo": "  MI-NUEVA-APP  ",
                    "nombre": "Mi Nueva App"
                }
                """;

        mockMvc.perform(
                        post("/api/aplicaciones")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "auth-admin"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.codigo")
                                .value("mi-nueva-app")
                );

        assertTrue(
                aplicacionRepository
                        .findByCodigo("mi-nueva-app")
                        .isPresent()
        );
    }

    @Test
    void crearAplicacionConCodigoInvalidoDebeRetornar400() throws Exception {

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "codigo": "mi_app",
                "nombre": "Mi App"
            }
            """;

        mockMvc.perform(
                        post("/api/aplicaciones")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "auth-admin"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.codigo")
                                .value("El código solo puede contener letras, números y guiones")
                );
    }
    @Test
    void crearAplicacionConCodigoDuplicadoNormalizadoDebeRetornar409() throws Exception {

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String primeraAplicacion = """
            {
                "codigo": "app-duplicada",
                "nombre": "Primera App"
            }
            """;

        String segundaAplicacion = """
            {
                "codigo": "  APP-DUPLICADA  ",
                "nombre": "Segunda App"
            }
            """;

        mockMvc.perform(
                        post("/api/aplicaciones")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "auth-admin"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(primeraAplicacion)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/aplicaciones")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "auth-admin"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segundaAplicacion)
                )
                .andExpect(status().isConflict());
    }

    //TEST DE CRUD DE APLICACIONES
    @Test
    void superAdminDebeCrearAplicacion() throws Exception {

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "codigo": "ventas-app",
                "nombre": "Ventas App"
            }
            """;

        mockMvc.perform(
                        post("/api/aplicaciones")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "auth-admin"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.codigo").value("ventas-app"))
                .andExpect(jsonPath("$.nombre").value("Ventas App"))
                .andExpect(jsonPath("$.activo").value(true));

        assertTrue(
                aplicacionRepository
                        .findByCodigo("ventas-app")
                        .isPresent()
        );
    }
    @Test
    void superAdminDebeListarAplicaciones() throws Exception {

        Aplicacion aplicacion = new Aplicacion(
                "app-listado-test",
                "Aplicacion Listado Test"
        );

        aplicacionRepository.save(aplicacion);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        mockMvc.perform(
                        get("/api/aplicaciones")
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
                                .value(hasItem("app-listado-test"))
                )
                .andExpect(
                        jsonPath("$[*].nombre")
                                .value(hasItem("Aplicacion Listado Test"))
                );
    }
    @Test
    void superAdminDebeBuscarAplicacionPorId() throws Exception {

        Aplicacion aplicacion = new Aplicacion(
                "app-busqueda-test",
                "Aplicacion Busqueda Test"
        );

        Aplicacion guardada = aplicacionRepository.save(aplicacion);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        mockMvc.perform(
                        get("/api/aplicaciones/{id}", guardada.getId())
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
                .andExpect(jsonPath("$.id").value(guardada.getId()))
                .andExpect(jsonPath("$.codigo").value("app-busqueda-test"))
                .andExpect(jsonPath("$.nombre").value("Aplicacion Busqueda Test"))
                .andExpect(jsonPath("$.activo").value(true));
    }
    @Test
    void buscarAplicacionInexistenteDebeRetornar404() throws Exception {

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        mockMvc.perform(
                        get("/api/aplicaciones/{id}", 999999L)
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
    void superAdminDebeEditarNombreDeAplicacion() throws Exception {

        Aplicacion aplicacion = new Aplicacion(
                "app-edicion-test",
                "Nombre Original"
        );

        Aplicacion guardada = aplicacionRepository.save(aplicacion);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "nombre": "Nombre Actualizado"
            }
            """;

        mockMvc.perform(
                        put("/api/aplicaciones/{id}", guardada.getId())
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "auth-admin"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardada.getId()))
                .andExpect(jsonPath("$.codigo").value("app-edicion-test"))
                .andExpect(jsonPath("$.nombre").value("Nombre Actualizado"))
                .andExpect(jsonPath("$.activo").value(true));
    }
    @Test
    void editarAplicacionInexistenteDebeRetornar404() throws Exception {

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "nombre": "Nuevo Nombre"
            }
            """;

        mockMvc.perform(
                        put("/api/aplicaciones/{id}", 999999L)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "auth-admin"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void superAdminDebeDesactivarAplicacion() throws Exception {

        Aplicacion aplicacion = new Aplicacion(
                "app-desactivar-test",
                "App Desactivar Test"
        );

        Aplicacion guardada = aplicacionRepository.save(aplicacion);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "activo": false
            }
            """;

        mockMvc.perform(
                        patch("/api/aplicaciones/{id}/estado", guardada.getId())
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardada.getId()))
                .andExpect(jsonPath("$.codigo").value("app-desactivar-test"))
                .andExpect(jsonPath("$.activo").value(false));
    }
    @Test
    void superAdminDebeActivarAplicacion() throws Exception {

        Aplicacion aplicacion = new Aplicacion(
                "app-activar-test",
                "App Activar Test"
        );

        aplicacion.setActivo(false);

        Aplicacion guardada = aplicacionRepository.save(aplicacion);

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "activo": true
            }
            """;

        mockMvc.perform(
                        patch("/api/aplicaciones/{id}/estado", guardada.getId())
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }
    @Test
    void noDebePermitirDesactivarAuthAdmin() throws Exception {

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

        String token = jwtService.generarToken(
                "superadmin@test.com",
                "ADMIN",
                "auth-admin",
                true
        );

        String json = """
            {
                "activo": false
            }
            """;

        mockMvc.perform(
                        patch("/api/aplicaciones/{id}/estado", authAdmin.getId())
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict());
    }

    @Test
    void desactivarAplicacionDebeRevocarSoloSusRefreshTokens() throws Exception {

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Refresh Test");
        usuario.setEmail("refresh-app-test@test.com");
        usuario.setPassword("password-test");

        usuario = usuarioRepository.save(usuario);

        Aplicacion appA = new Aplicacion(
                "refresh-app-a",
                "Refresh App A"
        );

        Aplicacion appB = new Aplicacion(
                "refresh-app-b",
                "Refresh App B"
        );

        appA = aplicacionRepository.save(appA);
        appB = aplicacionRepository.save(appB);

        RefreshToken tokenA1 = new RefreshToken();
        tokenA1.setTokenHash("hash-refresh-app-a-1");
        tokenA1.setUsuario(usuario);
        tokenA1.setAplicacion(appA);
        tokenA1.setCreadoEn(Instant.now());
        tokenA1.setExpiraEn(Instant.now().plusSeconds(3600));
        tokenA1.setRevocado(false);

        RefreshToken tokenA2 = new RefreshToken();
        tokenA2.setTokenHash("hash-refresh-app-a-2");
        tokenA2.setUsuario(usuario);
        tokenA2.setAplicacion(appA);
        tokenA2.setCreadoEn(Instant.now());
        tokenA2.setExpiraEn(Instant.now().plusSeconds(3600));
        tokenA2.setRevocado(false);

        RefreshToken tokenB = new RefreshToken();
        tokenB.setTokenHash("hash-refresh-app-b");
        tokenB.setUsuario(usuario);
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

        String json = """
            {
                "activo": false
            }
            """;

        mockMvc.perform(
                        patch("/api/aplicaciones/{id}/estado", appA.getId())
                                .header("Authorization", "Bearer " + token)
                                .header("X-App-Id", "auth-admin")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));

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
}