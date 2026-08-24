package com.todoapp.todo_app.controller;
import com.todoapp.todo_app.dto.LogoutRequest;
import com.todoapp.todo_app.dto.*;
import com.todoapp.todo_app.entity.Usuario;
import com.todoapp.todo_app.service.AuthService;
import com.todoapp.todo_app.service.JwtService;
import com.todoapp.todo_app.service.LoginAttemptService;
import com.todoapp.todo_app.service.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(
            AuthService authService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            LoginAttemptService loginAttemptService,
            RefreshTokenService refreshTokenService
    ) {
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

        if (loginAttemptService.estaBloqueado(request.getEmail())) {
            return ResponseEntity.status(429)
                    .body("Demasiados intentos fallidos. Intenta de nuevo en unos minutos.");
        }

        Usuario usuario = authService.buscarPorEmail(request.getEmail());

        boolean credencialesValidas = usuario != null
                && passwordEncoder.matches(request.getPassword(), usuario.getPassword());

        if (!credencialesValidas) {
            loginAttemptService.registrarFallo(request.getEmail());
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

        // Aunque la contraseña sea correcta, si el usuario no tiene
        // acceso a esta app, se rechaza (mismo mensaje genérico para
        // no revelar si el problema fue la contraseña o el acceso).
        if (!usuario.tieneAcceso(request.getApp())) {
            loginAttemptService.registrarFallo(request.getEmail());
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }

        loginAttemptService.registrarExito(request.getEmail());

        String accessToken = jwtService.generarToken(usuario.getEmail(), usuario.getRol(), request.getApp());
        String refreshToken = refreshTokenService.crear(usuario);

        PerfilResponse perfil = new PerfilResponse(
                usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol()
        );

        return ResponseEntity.ok(new LoginResponse(accessToken, refreshToken, perfil));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshRequest request) {

        var resultadoOpt = refreshTokenService.rotar(request.getRefreshToken());

        if (resultadoOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Refresh token inválido o expirado");
        }

        var resultado = resultadoOpt.get();
        Usuario usuario = resultado.usuario();

        // Se revalida el acceso a la app: si en el camino le quitaron
        // el acceso, el refresh no le regala un token nuevo igual.
        if (!usuario.tieneAcceso(request.getApp())) {
            return ResponseEntity.status(401).body("No tienes acceso a esta aplicación");
        }

        String nuevoAccessToken = jwtService.generarToken(usuario.getEmail(), usuario.getRol(), request.getApp());

        PerfilResponse perfil = new PerfilResponse(
                usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol()
        );

        LoginResponse response = new LoginResponse(
                nuevoAccessToken, resultado.nuevoRefreshToken(), perfil
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request) {
        refreshTokenService.revocar(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(new ValidateResponse(false, null, null));
        }

        String token = authorizationHeader.substring(7);

        if (!jwtService.tokenValido(token)) {
            return ResponseEntity.status(401).body(new ValidateResponse(false, null, null));
        }

        String email = jwtService.extraerEmail(token);
        String rol = jwtService.extraerRol(token);

        return ResponseEntity.ok(new ValidateResponse(true, email, rol));
    }
}