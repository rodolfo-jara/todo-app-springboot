package com.todoapp.todo_app.controller;


import com.todoapp.todo_app.entity.Aplicacion;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.entity.UsuarioAplicacion;
import com.todoapp.todo_app.repository.AplicacionRepository;
import com.todoapp.todo_app.repository.RefreshTokenRepository;
import com.todoapp.todo_app.repository.UsuarioAplicacionRepository;
import com.todoapp.todo_app.repository.UsuarioRepository;
import com.todoapp.todo_app.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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

    @BeforeEach
    void prepararDatos() {

        refreshTokenRepository.deleteAll();
        usuarioAplicacionRepository.deleteAll();
        aplicacionRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario Test");
        usuario.setEmail("test@test.com");
        usuario.setPassword(passwordEncoder.encode("Password123"));
        usuario.setRol("USER");
        usuario.getAplicaciones().add("todo-app");

        usuarioRepository.save(usuario);
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
    void usuarioConRolUserNoDebeAccederAEndpointAdmin() throws Exception {

        String loginJson = """
            {
                "email": "test@test.com",
                "password": "Password123",
                "app": "todo-app"
            }
            """;

        String respuestaLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new tools.jackson.databind.ObjectMapper()
                .readTree(respuestaLogin)
                .get("token")
                .asText();

        mockMvc.perform(get("/api/perfil/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
    @Test
    void usuarioConRolAdminDebeAccederAEndpointAdmin() throws Exception {

        Usuario admin = usuarioRepository.findByEmail("test@test.com")
                .orElseThrow();

        admin.setRol("ADMIN");
        usuarioRepository.save(admin);

        String loginJson = """
            {
                "email": "test@test.com",
                "password": "Password123",
                "app": "todo-app"
            }
            """;

        String respuestaLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new tools.jackson.databind.ObjectMapper()
                .readTree(respuestaLogin)
                .get("token")
                .asText();

        mockMvc.perform(get("/api/perfil/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
    @Test
    void registroDebeNormalizarEmail() throws Exception {

        refreshTokenRepository.deleteAll();
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

        Usuario usuarioGuardado = usuarioRepository.findByEmail("test@test.com")
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
                "otra-app"
        );

        mockMvc.perform(get("/api/perfil")
                        .header("Authorization", "Bearer " + token))
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
                new UsuarioAplicacion(usuario, miapp, "USER");

        UsuarioAplicacion accesoDemoApp =
                new UsuarioAplicacion(usuario, demoapp, "ADMIN");

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
}