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
}