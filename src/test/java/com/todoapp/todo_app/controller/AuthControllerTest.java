package com.todoapp.todo_app.controller;


import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.repository.RefreshTokenRepository;
import com.todoapp.todo_app.repository.UsuarioRepository;
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

    @BeforeEach
    void prepararDatos() {

        refreshTokenRepository.deleteAll();
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
}