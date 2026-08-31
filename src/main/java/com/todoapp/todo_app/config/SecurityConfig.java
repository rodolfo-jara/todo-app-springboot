package com.todoapp.todo_app.config;

import com.todoapp.todo_app.repository.UsuarioAplicacionRepository;
import com.todoapp.todo_app.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    private final JwtService jwtService;
    private final UsuarioAplicacionRepository usuarioAplicacionRepository;

    public SecurityConfig(
            JwtService jwtService,
            UsuarioAplicacionRepository usuarioAplicacionRepository
    ) {
        this.jwtService = jwtService;
        this.usuarioAplicacionRepository = usuarioAplicacionRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(
                        jwtService,
                        usuarioAplicacionRepository
                );

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Solo login queda público de /api/auth.
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/validate").permitAll()
                        // Registro público, pero solo vía RegistroRequest (sin id/rol).
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()
                                // CRUD global de aplicaciones: solo SUPER_ADMIN
                                .requestMatchers("/api/aplicaciones/**")
                                .hasRole("SUPER_ADMIN")

                                // Administración global de usuarios: solo SUPER_ADMIN
                                .requestMatchers("/api/usuarios/**")
                                .hasRole("SUPER_ADMIN")

                                // Administración dentro de una aplicación: solo ADMIN
                                .requestMatchers("/api/admin/**")
                                .hasRole("ADMIN")

                                .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                        )
                )
                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

}
