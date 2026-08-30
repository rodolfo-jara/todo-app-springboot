package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.entity.Aplicacion;
import com.todoapp.todo_app.entity.RolAplicacion;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.entity.UsuarioAplicacion;
import com.todoapp.todo_app.repository.AplicacionRepository;
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


}