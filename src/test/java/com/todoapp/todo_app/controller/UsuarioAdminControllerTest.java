package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.repository.UsuarioRepository;
import com.todoapp.todo_app.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
}