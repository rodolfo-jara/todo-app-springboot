package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.repository.AplicacionRepository;
import com.todoapp.todo_app.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}