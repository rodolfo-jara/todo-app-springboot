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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthValidateTest {

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

    private Usuario usuario;
    private Aplicacion aplicacion;
    private UsuarioAplicacion acceso;

    @BeforeEach
    void prepararDatos() {

        refreshTokenRepository.deleteAll();
        usuarioAplicacionRepository.deleteAll();
        aplicacionRepository.deleteAll();
        usuarioRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNombre("Usuario Validate");
        usuario.setEmail("validate@test.com");
        usuario.setPassword("password-test");
        usuario.setSuperAdmin(false);

        usuario = usuarioRepository.save(usuario);

        aplicacion = new Aplicacion(
                "validate-app",
                "Validate App"
        );

        aplicacion = aplicacionRepository.save(aplicacion);

        acceso = new UsuarioAplicacion(
                usuario,
                aplicacion,
                RolAplicacion.USER
        );

        acceso = usuarioAplicacionRepository.save(acceso);
    }

    @Test
    void validateDebeAceptarTokenConEstadoActualValido()
            throws Exception {

        String token = jwtService.generarToken(
                usuario.getEmail(),
                "USER",
                "validate-app",
                false
        );

        mockMvc.perform(
                        get("/api/auth/validate")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "validate-app"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true))
                .andExpect(
                        jsonPath("$.email")
                                .value("validate@test.com")
                )
                .andExpect(jsonPath("$.rol").value("USER"));
    }

    @Test
    void validateDebeRechazarAccesoDesactivado()
            throws Exception {

        String token = jwtService.generarToken(
                usuario.getEmail(),
                "USER",
                "validate-app",
                false
        );

        acceso.setActivo(false);
        usuarioAplicacionRepository.save(acceso);

        mockMvc.perform(
                        get("/api/auth/validate")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "validate-app"
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valido").value(false));
    }

    @Test
    void validateDebeRechazarAplicacionDesactivada()
            throws Exception {

        String token = jwtService.generarToken(
                usuario.getEmail(),
                "USER",
                "validate-app",
                false
        );

        aplicacion.setActivo(false);
        aplicacionRepository.save(aplicacion);

        mockMvc.perform(
                        get("/api/auth/validate")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "validate-app"
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valido").value(false));
    }

    @Test
    void validateDebeRechazarTokenConRolDesactualizado()
            throws Exception {

        String token = jwtService.generarToken(
                usuario.getEmail(),
                "USER",
                "validate-app",
                false
        );

        acceso.setRol("ADMIN");
        usuarioAplicacionRepository.save(acceso);

        mockMvc.perform(
                        get("/api/auth/validate")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "validate-app"
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valido").value(false));
    }

    @Test
    void validateDebeRechazarSuperAdminDesactualizado()
            throws Exception {

        String token = jwtService.generarToken(
                usuario.getEmail(),
                "USER",
                "validate-app",
                false
        );

        usuario.setSuperAdmin(true);
        usuarioRepository.save(usuario);

        mockMvc.perform(
                        get("/api/auth/validate")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "validate-app"
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valido").value(false));
    }

    @Test
    void validateDebeRechazarAppDistintaAlAudience()
            throws Exception {

        String token = jwtService.generarToken(
                usuario.getEmail(),
                "USER",
                "validate-app",
                false
        );

        mockMvc.perform(
                        get("/api/auth/validate")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .header(
                                        "X-App-Id",
                                        "otra-app"
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validateDebeRechazarTokenSinXAppId()
            throws Exception {

        String token = jwtService.generarToken(
                usuario.getEmail(),
                "USER",
                "validate-app",
                false
        );

        mockMvc.perform(
                        get("/api/auth/validate")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }
}