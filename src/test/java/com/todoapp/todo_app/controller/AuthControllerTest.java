package com.todoapp.todo_app.controller;


import com.todoapp.todo_app.entity.*;
import com.todoapp.todo_app.repository.AplicacionRepository;
import com.todoapp.todo_app.repository.RefreshTokenRepository;
import com.todoapp.todo_app.repository.UsuarioAplicacionRepository;
import com.todoapp.todo_app.repository.UsuarioRepository;
import com.todoapp.todo_app.service.JwtService;
import com.todoapp.todo_app.service.RefreshTokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private JwtService jwtService;

    @Autowired
    private AplicacionRepository aplicacionRepository;

    @Autowired
    private UsuarioAplicacionRepository usuarioAplicacionRepository;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void prepararDatos() {

        refreshTokenRepository.deleteAll();
        usuarioAplicacionRepository.deleteAll();
        aplicacionRepository.deleteAll();
        usuarioRepository.deleteAll();

        // 1. Crear usuario
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Test");
        usuario.setEmail("test@test.com");
        usuario.setPassword(passwordEncoder.encode("Password123"));

        usuario = usuarioRepository.save(usuario);

        // 2. Crear la aplicación
        Aplicacion todoApp = new Aplicacion(
                "todo-app",
                "Todo App"
        );

        todoApp = aplicacionRepository.save(todoApp);

        // 3. Dar acceso del usuario a todo-app
        UsuarioAplicacion acceso = new UsuarioAplicacion(
                usuario,
                todoApp,
                RolAplicacion.USER
        );

        usuarioAplicacionRepository.save(acceso);
    }

    @Test
    void loginConUsuarioInexistenteDebeRetornar401() throws Exception {

        String json = """
                {
                    "email": "noexiste@test.com",
                    "password": "Password123",
                    "app": "todo-app"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void loginCorrectoDebeRetornar200() throws Exception {

        String json = """
            {
                "email": "test@test.com",
                "password": "Password123",
                "app": "todo-app"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }
    @Test
    void loginConPasswordIncorrectaDebeRetornar401() throws Exception {

        String json = """
            {
                "email": "test@test.com",
                "password": "PasswordMala123",
                "app": "todo-app"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void accesoSinJwtDebeRetornar401() throws Exception {

        mockMvc.perform(get("/api/perfil"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void registroDebeNormalizarEmail() throws Exception {

        // Limpiamos primero tablas hijas y luego tablas padre
        refreshTokenRepository.deleteAll();
        usuarioAplicacionRepository.deleteAll();
        usuarioRepository.deleteAll();

        String json = """
            {
                "nombre": "Usuario Normalizado",
                "email": "  TEST@TEST.COM  ",
                "password": "Password123",
                "app": "todo-app"
            }
            """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        Usuario usuarioGuardado = usuarioRepository
                .findByEmail("test@test.com")
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                "test@test.com",
                usuarioGuardado.getEmail()
        );
    }
    @Test
    void loginDebeNormalizarEmail() throws Exception {

        String json = """
            {
                "email": "  TEST@TEST.COM  ",
                "password": "Password123",
                "app": "todo-app"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }
    @Test
    void registroConEmailDuplicadoDebeRetornar409() throws Exception {

        String json = """
            {
                "nombre": "Otro Usuario",
                "email": "TEST@TEST.COM",
                "password": "OtraPassword123",
                "app": "todo-app"
            }
            """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }
    @Test
    void loginNoDebeExponerPassword() throws Exception {

        String json = """
            {
                "email": "test@test.com",
                "password": "Password123",
                "app": "todo-app"
            }
            """;

        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.id").exists())
                .andExpect(jsonPath("$.usuario.nombre").value("Usuario Test"))
                .andExpect(jsonPath("$.usuario.email").value("test@test.com"))
                .andExpect(jsonPath("$.usuario.rol").value("USER"))
                .andExpect(jsonPath("$.usuario.password").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println("RESPUESTA DEL LOGIN:");
        System.out.println(respuesta);
}
    @Test
    void respuestaLoginNoDebePermitirObtenerPassword() throws Exception {

        String json = """
            {
                "email": "test@test.com",
                "password": "Password123",
                "app": "todo-app"
            }
            """;

        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var jsonRespuesta = new tools.jackson.databind.ObjectMapper()
                .readTree(respuesta);

        var usuario = jsonRespuesta.get("usuario");
        var password = usuario.get("password");

        System.out.println("Usuario recibido: " + usuario);
        System.out.println("Intento de obtener password: " + password);

        org.junit.jupiter.api.Assertions.assertNull(password);
    }
    @Test
    void tokenDeOtraAppDebeSerRechazado() throws Exception {

        String token = jwtService.generarToken(
                "test@test.com",
                "USER",
                "miapp"
        );

        mockMvc.perform(get("/api/perfil")
                        .header("Authorization", "Bearer " + token)
                        .header("X-App-Id", "demoapp"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void usuarioPuedeTenerRolesDistintosPorAplicacion() {

        Usuario usuario = usuarioRepository.findByEmail("test@test.com")
                .orElseThrow();

        Aplicacion miapp = new Aplicacion("miapp", "Mi App");
        Aplicacion demoapp = new Aplicacion("demoapp", "Demo App");

        aplicacionRepository.save(miapp);
        aplicacionRepository.save(demoapp);

        UsuarioAplicacion accesoMiApp =
                new UsuarioAplicacion(
                        usuario,
                        miapp,
                        RolAplicacion.USER
                );

        UsuarioAplicacion accesoDemoApp =
                new UsuarioAplicacion(
                        usuario,
                        demoapp,
                        RolAplicacion.ADMIN
                );

        usuarioAplicacionRepository.save(accesoMiApp);
        usuarioAplicacionRepository.save(accesoDemoApp);

        UsuarioAplicacion relacionMiApp =
                usuarioAplicacionRepository
                        .findByUsuarioEmailAndAplicacionCodigo(
                                "test@test.com",
                                "miapp"
                        )
                        .orElseThrow();

        UsuarioAplicacion relacionDemoApp =
                usuarioAplicacionRepository
                        .findByUsuarioEmailAndAplicacionCodigo(
                                "test@test.com",
                                "demoapp"
                        )
                        .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                "USER",
                relacionMiApp.getRol()
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                "ADMIN",
                relacionDemoApp.getRol()
        );
    }

    @Test
    void loginDebeUsarRolDeLaAplicacion() throws Exception {

        Usuario usuario = usuarioRepository.findByEmail("test@test.com")
                .orElseThrow();

        // El @BeforeEach ya creó:
        // todo-app -> USER
        Aplicacion demoApp = new Aplicacion(
                "demoapp",
                "Demo App"
        );

        demoApp = aplicacionRepository.save(demoApp);

        UsuarioAplicacion accesoDemo = new UsuarioAplicacion(
                usuario,
                demoApp,
                RolAplicacion.ADMIN
        );

        usuarioAplicacionRepository.save(accesoDemo);

        String json = """
            {
                "email": "test@test.com",
                "password": "Password123",
                "app": "demoapp"
            }
            """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.email")
                        .value("test@test.com"))
                .andExpect(jsonPath("$.usuario.rol")
                        .value("ADMIN"));
    }
    @Test
    void loginDebeGenerarTokenConAudienceDeLaAplicacion() throws Exception {

        Usuario usuario = usuarioRepository.findByEmail("test@test.com")
                .orElseThrow();

        Aplicacion demoApp = new Aplicacion(
                "demoapp",
                "Demo App"
        );

        demoApp = aplicacionRepository.save(demoApp);

        UsuarioAplicacion accesoDemo = new UsuarioAplicacion(
                usuario,
                demoApp,
                RolAplicacion.ADMIN
        );

        usuarioAplicacionRepository.save(accesoDemo);

        String json = """
            {
                "email": "test@test.com",
                "password": "Password123",
                "app": "demoapp"
            }
            """;

        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new tools.jackson.databind.ObjectMapper()
                .readTree(respuesta)
                .get("token")
                .asText();

        String audience = jwtService.extraerApp(token);

        org.junit.jupiter.api.Assertions.assertEquals(
                "demoapp",
                audience
        );
    }
    @Test
    void tokenFirmadoConOtraClaveDebeSerRechazado() throws Exception {

        SecretKey claveFalsa = Keys.hmacShaKeyFor(
                "esta-es-una-clave-falsa-muy-larga-123456789012345678901234567890"
                        .getBytes(StandardCharsets.UTF_8)
        );

        String tokenFalso = Jwts.builder()
                .subject("test@test.com")
                .claim("rol", "ADMIN")
                .audience()
                .add("todo-app")
                .and()
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(claveFalsa)
                .compact();

        mockMvc.perform(get("/api/perfil")
                        .header("Authorization", "Bearer " + tokenFalso)
                        .header("X-App-Id", "todo-app"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void tokenExpiradoDebeSerRechazado() throws Exception {

        String token = jwtService.generarTokenExpiradoParaTest(
                "test@test.com",
                "USER",
                "todo-app"
        );

        mockMvc.perform(get("/api/perfil")
                        .header("Authorization", "Bearer " + token)
                        .header("X-App-Id", "todo-app"))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void rolEnRequestNoDebeCambiarRolRealDelUsuario() throws Exception {

        String json = """
            {
                "email": "test@test.com",
                "password": "Password123",
                "app": "todo-app",
                "rol": "ADMIN"
            }
            """;

        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.rol").value("USER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new tools.jackson.databind.ObjectMapper()
                .readTree(respuesta)
                .get("token")
                .asText();

        org.junit.jupiter.api.Assertions.assertEquals(
                "USER",
                jwtService.extraerRol(token)
        );
        mockMvc.perform(get("/api/admin/usuarios")
                        .header("Authorization", "Bearer " + token)
                        .header("X-App-Id", "todo-app"))
                .andExpect(status().isForbidden());
    }
    @Test
    void registroDebeCrearUsuarioAplicacion() throws Exception {

        String json = """
        {
            "nombre": "Nuevo Usuario",
            "email": "nuevo@test.com",
            "password": "Password123",
            "app": "todo-app"
        }
        """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        UsuarioAplicacion acceso = usuarioAplicacionRepository
                .findByUsuarioEmailAndAplicacionCodigo(
                        "nuevo@test.com",
                        "todo-app"
                )
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                "USER",
                acceso.getRol()
        );

        assertTrue(
                acceso.isActivo()
        );
    }
    @Test
    void registroConAplicacionInexistenteDebeRetornar400() throws Exception {

        String json = """
        {
            "nombre": "Usuario App Inexistente",
            "email": "inexistente@test.com",
            "password": "Password123",
            "app": "app-que-no-existe"
        }
        """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        assertTrue(
                usuarioRepository
                        .findByEmail("inexistente@test.com")
                        .isEmpty()
        );
    }
    @Test
    void registroConAplicacionDesactivadaDebeRetornar400() throws Exception {

        Aplicacion aplicacion = aplicacionRepository
                .findByCodigo("todo-app")
                .orElseThrow();

        aplicacion.setActivo(false);
        aplicacionRepository.save(aplicacion);

        String json = """
        {
            "nombre": "Usuario App Desactivada",
            "email": "desactivada@test.com",
            "password": "Password123",
            "app": "todo-app"
        }
        """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        assertTrue(
                usuarioRepository
                        .findByEmail("desactivada@test.com")
                        .isEmpty()
        );
    }

    @Test
    void usuarioExistenteNoDebePoderAutoAsignarseAOtraAplicacion() throws Exception {

        Aplicacion demoApp = new Aplicacion(
                "demoapp",
                "Demo App"
        );

        aplicacionRepository.save(demoApp);

        String json = """
        {
            "nombre": "Usuario Test",
            "email": "test@test.com",
            "password": "Password123",
            "app": "demoapp"
        }
        """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());

        assertTrue(
                usuarioAplicacionRepository
                        .findByUsuarioEmailAndAplicacionCodigo(
                                "test@test.com",
                                "demoapp"
                        )
                        .isEmpty()
        );
    }
    @Test
    void registroNoDebePermitirAutoAsignarseAdmin() throws Exception {

        String json = """
        {
            "nombre": "Usuario Malicioso",
            "email": "malicioso@test.com",
            "password": "Password123",
            "app": "todo-app",
            "rol": "ADMIN"
        }
        """;

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        UsuarioAplicacion acceso = usuarioAplicacionRepository
                .findByUsuarioEmailAndAplicacionCodigo(
                        "malicioso@test.com",
                        "todo-app"
                )
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                "USER",
                acceso.getRol()
        );
    }
    @Test
    void loginConAplicacionDesactivadaDebeRetornar401() throws Exception {

        Aplicacion aplicacion = aplicacionRepository
                .findByCodigo("todo-app")
                .orElseThrow();

        aplicacion.setActivo(false);
        aplicacionRepository.save(aplicacion);

        String json = """
        {
            "email": "test@test.com",
            "password": "Password123",
            "app": "todo-app"
        }
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void loginConAplicacionInexistenteDebeRetornar401() throws Exception {

        String json = """
        {
            "email": "test@test.com",
            "password": "Password123",
            "app": "app-que-no-existe"
        }
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void loginConAccesoDesactivadoDebeRetornar401() throws Exception {

        UsuarioAplicacion acceso = usuarioAplicacionRepository
                .findByUsuarioEmailAndAplicacionCodigo(
                        "test@test.com",
                        "todo-app"
                )
                .orElseThrow();

        acceso.setActivo(false);
        usuarioAplicacionRepository.save(acceso);

        String json = """
        {
            "email": "test@test.com",
            "password": "Password123",
            "app": "todo-app"
        }
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void loginNoDebeRevelarMotivoDeCredencialesInvalidas() throws Exception {

        String json = """
        {
            "email": "test@test.com",
            "password": "PasswordIncorrecta",
            "app": "todo-app"
        }
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciales inválidas"));
    }
    @Test
    void loginDebeNormalizarCodigoAplicacion() throws Exception {

        String json = """
        {
            "email": "test@test.com",
            "password": "Password123",
            "app": "  TODO-APP  "
        }
        """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void perfilDebeMostrarRolDeLaAplicacionActual() throws Exception {

        Usuario usuario = usuarioRepository
                .findByEmail("test@test.com")
                .orElseThrow();

        Aplicacion demoApp = new Aplicacion(
                "demoapp",
                "Demo App"
        );

        demoApp = aplicacionRepository.save(demoApp);

        UsuarioAplicacion accesoDemo = new UsuarioAplicacion(
                usuario,
                demoApp,
                RolAplicacion.ADMIN
        );

        usuarioAplicacionRepository.save(accesoDemo);

        String loginJson = """
        {
            "email": "test@test.com",
            "password": "Password123",
            "app": "demoapp"
        }
        """;

        String respuestaLogin = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new tools.jackson.databind.ObjectMapper()
                .readTree(respuestaLogin)
                .get("token")
                .asText();

        mockMvc.perform(
                        get("/api/perfil")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "demoapp"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }


    @Test
    void superAdminDebeAccederAAplicaciones() throws Exception {

        Usuario usuario = usuarioRepository
                .findByEmail("test@test.com")
                .orElseThrow();

        usuario.setSuperAdmin(true);
        usuarioRepository.save(usuario);

        Aplicacion authAdmin = new Aplicacion(
                "auth-admin",
                "Authentication Admin"
        );

        authAdmin = aplicacionRepository.save(authAdmin);

        UsuarioAplicacion accesoAdmin = new UsuarioAplicacion(
                usuario,
                authAdmin,
                RolAplicacion.ADMIN
        );

        usuarioAplicacionRepository.save(accesoAdmin);

        String loginJson = """
        {
            "email": "test@test.com",
            "password": "Password123",
            "app": "auth-admin"
        }
        """;

        String respuesta = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new tools.jackson.databind.ObjectMapper()
                .readTree(respuesta)
                .get("token")
                .asText();

        assertTrue(
                jwtService.extraerSuperAdmin(token)
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
                .andExpect(status().isOk());
    }
    @Test
    void adminNormalNoDebeAccederAAplicaciones() throws Exception {

        UsuarioAplicacion acceso = usuarioAplicacionRepository
                .findByUsuarioEmailAndAplicacionCodigo(
                        "test@test.com",
                        "todo-app"
                )
                .orElseThrow();

        acceso.setRol("ADMIN");
        usuarioAplicacionRepository.save(acceso);

        String loginJson = """
        {
            "email": "test@test.com",
            "password": "Password123",
            "app": "todo-app"
        }
        """;

        String respuesta = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new tools.jackson.databind.ObjectMapper()
                .readTree(respuesta)
                .get("token")
                .asText();

        org.junit.jupiter.api.Assertions.assertFalse(
                jwtService.extraerSuperAdmin(token)
        );

        mockMvc.perform(
                        get("/api/aplicaciones")
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
    void userNormalNoDebeAccederAAplicaciones() throws Exception {

        String loginJson = """
        {
            "email": "test@test.com",
            "password": "Password123",
            "app": "todo-app"
        }
        """;

        String respuesta = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new tools.jackson.databind.ObjectMapper()
                .readTree(respuesta)
                .get("token")
                .asText();

        mockMvc.perform(
                        get("/api/aplicaciones")
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
    void refreshTokenDeUnaAppNoDebeServirParaOtraApp() throws Exception {

        Aplicacion demoApp = new Aplicacion(
                "demoapp",
                "Demo App"
        );

        aplicacionRepository.save(demoApp);

        String loginJson = """
        {
            "email": "test@test.com",
            "password": "Password123",
            "app": "todo-app"
        }
        """;

        String respuestaLogin = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = new tools.jackson.databind.ObjectMapper()
                .readTree(respuestaLogin)
                .get("refreshToken")
                .asText();

        String refreshIncorrectoJson = """
        {
            "refreshToken": "%s",
            "app": "demoapp"
        }
        """.formatted(refreshToken);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshIncorrectoJson)
                )
                .andExpect(status().isUnauthorized());

        // El intento incorrecto NO debe revocar el token original
        String refreshCorrectoJson = """
        {
            "refreshToken": "%s",
            "app": "todo-app"
        }
        """.formatted(refreshToken);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshCorrectoJson)
                )
                .andExpect(status().isOk());
    }
    @Test
    void refreshConAplicacionDesactivadaDebeRetornar401() throws Exception {

        String loginJson = """
        {
            "email": "test@test.com",
            "password": "Password123",
            "app": "todo-app"
        }
        """;

        String respuestaLogin = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = new tools.jackson.databind.ObjectMapper()
                .readTree(respuestaLogin)
                .get("refreshToken")
                .asText();

        Aplicacion aplicacion = aplicacionRepository
                .findByCodigo("todo-app")
                .orElseThrow();

        aplicacion.setActivo(false);
        aplicacionRepository.save(aplicacion);

        String refreshJson = """
        {
            "refreshToken": "%s",
            "app": "todo-app"
        }
        """.formatted(refreshToken);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshJson)
                )
                .andExpect(status().isUnauthorized());
    }
    @Test
    void refreshConAccesoDesactivadoDebeRetornar401() throws Exception {

        String loginJson = """
        {
            "email": "test@test.com",
            "password": "Password123",
            "app": "todo-app"
        }
        """;

        String respuestaLogin = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshToken = new tools.jackson.databind.ObjectMapper()
                .readTree(respuestaLogin)
                .get("refreshToken")
                .asText();

        UsuarioAplicacion acceso = usuarioAplicacionRepository
                .findByUsuarioEmailAndAplicacionCodigo(
                        "test@test.com",
                        "todo-app"
                )
                .orElseThrow();

        acceso.setActivo(false);
        usuarioAplicacionRepository.save(acceso);

        String refreshJson = """
        {
            "refreshToken": "%s",
            "app": "todo-app"
        }
        """.formatted(refreshToken);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshJson)
                )
                .andExpect(status().isUnauthorized());
    }
    @Test
    void refreshTokenRotadoNoDebePoderReutilizarse() throws Exception {

        String loginJson = """
        {
            "email": "test@test.com",
            "password": "Password123",
            "app": "todo-app"
        }
        """;

        String respuestaLogin = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String refreshTokenOriginal =
                new tools.jackson.databind.ObjectMapper()
                        .readTree(respuestaLogin)
                        .get("refreshToken")
                        .asText();

        String primerRefreshJson = """
        {
            "refreshToken": "%s",
            "app": "todo-app"
        }
        """.formatted(refreshTokenOriginal);

        String respuestaRefresh = mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(primerRefreshJson)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String nuevoRefreshToken =
                new tools.jackson.databind.ObjectMapper()
                        .readTree(respuestaRefresh)
                        .get("refreshToken")
                        .asText();

        // El refresh original ya fue utilizado y debe estar revocado
        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(primerRefreshJson)
                )
                .andExpect(status().isUnauthorized());

        // El nuevo refresh sí debe funcionar
        String segundoRefreshJson = """
        {
            "refreshToken": "%s",
            "app": "todo-app"
        }
        """.formatted(nuevoRefreshToken);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(segundoRefreshJson)
                )
                .andExpect(status().isOk());
    }

    @Test
    void limpiarTokensDebeEliminarRevocadosYExpirados() {

        Usuario usuario = usuarioRepository
                .findByEmail("test@test.com")
                .orElseThrow();

        Aplicacion aplicacion = aplicacionRepository
                .findByCodigo("todo-app")
                .orElseThrow();

        // Token revocado
        RefreshToken revocado = new RefreshToken();
        revocado.setUsuario(usuario);
        revocado.setAplicacion(aplicacion);
        revocado.setTokenHash("hash-revocado");
        revocado.setCreadoEn(Instant.now());
        revocado.setExpiraEn(
                Instant.now().plusSeconds(3600)
        );
        revocado.setRevocado(true);

        refreshTokenRepository.save(revocado);

        // Token expirado
        RefreshToken expirado = new RefreshToken();
        expirado.setUsuario(usuario);
        expirado.setAplicacion(aplicacion);
        expirado.setTokenHash("hash-expirado");
        expirado.setCreadoEn(
                Instant.now().minusSeconds(7200)
        );
        expirado.setExpiraEn(
                Instant.now().minusSeconds(3600)
        );
        expirado.setRevocado(false);

        refreshTokenRepository.save(expirado);

        // Token válido: debe sobrevivir
        RefreshToken valido = new RefreshToken();
        valido.setUsuario(usuario);
        valido.setAplicacion(aplicacion);
        valido.setTokenHash("hash-valido");
        valido.setCreadoEn(Instant.now());
        valido.setExpiraEn(
                Instant.now().plusSeconds(3600)
        );
        valido.setRevocado(false);

        refreshTokenRepository.save(valido);

        long eliminados =
                refreshTokenService.limpiarTokens();

        org.junit.jupiter.api.Assertions.assertEquals(
                2,
                eliminados
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                1,
                refreshTokenRepository.count()
        );

        assertTrue(
                refreshTokenRepository
                        .findByTokenHash("hash-valido")
                        .isPresent()
        );
    }
    @Test
    void refreshTokenExpiradoDebeRetornar401() throws Exception {

        Usuario usuario = usuarioRepository
                .findByEmail("test@test.com")
                .orElseThrow();

        Aplicacion aplicacion = aplicacionRepository
                .findByCodigo("todo-app")
                .orElseThrow();

        String refreshToken =
                refreshTokenService.crear(
                        usuario,
                        aplicacion
                );

        RefreshToken tokenGuardado =
                refreshTokenRepository
                        .findAll()
                        .stream()
                        .findFirst()
                        .orElseThrow();

        tokenGuardado.setExpiraEn(
                Instant.now().minusSeconds(60)
        );

        refreshTokenRepository.save(tokenGuardado);

        String json = """
            {
                "refreshToken": "%s",
                "app": "todo-app"
            }
            """.formatted(refreshToken);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        content().string(
                                "Refresh token inválido o expirado"
                        )
                );
    }
    @Test
    void refreshTokenInexistenteDebeRetornar401() throws Exception {

        String json = """
            {
                "refreshToken": "refresh-token-manipulado-que-no-existe",
                "app": "todo-app"
            }
            """;

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        content().string(
                                "Refresh token inválido o expirado"
                        )
                );
    }
    @Test
    void logoutDebeRevocarRefreshTokenYNoPermitirReutilizarlo()
            throws Exception {

        Usuario usuario = usuarioRepository
                .findByEmail("test@test.com")
                .orElseThrow();

        Aplicacion aplicacion = aplicacionRepository
                .findByCodigo("todo-app")
                .orElseThrow();

        String refreshToken =
                refreshTokenService.crear(
                        usuario,
                        aplicacion
                );

        String logoutJson = """
            {
                "refreshToken": "%s"
            }
            """.formatted(refreshToken);

        mockMvc.perform(
                        post("/api/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(logoutJson)
                )
                .andExpect(status().isOk());

        RefreshToken tokenGuardado =
                refreshTokenRepository
                        .findAll()
                        .stream()
                        .findFirst()
                        .orElseThrow();

        assertTrue(tokenGuardado.isRevocado());

        String refreshJson = """
            {
                "refreshToken": "%s",
                "app": "todo-app"
            }
            """.formatted(refreshToken);

        mockMvc.perform(
                        post("/api/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(refreshJson)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        content().string(
                                "Refresh token inválido o expirado"
                        )
                );
    }

}