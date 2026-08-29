package com.todoapp.todo_app.config;

import com.todoapp.todo_app.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null ||
                !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        if (jwtService.tokenValido(token)) {

            String email = jwtService.extraerEmail(token);
            String rol = jwtService.extraerRol(token);
            String appToken = jwtService.extraerApp(token);

            boolean superAdmin =
                    jwtService.extraerSuperAdmin(token);

            String appRequest =
                    request.getHeader("X-App-Id");

            if (appRequest == null ||
                    !appRequest.equals(appToken)) {

                response.sendError(
                        HttpServletResponse.SC_UNAUTHORIZED,
                        "Token no válido para esta aplicación"
                );
                return;
            }

            List<GrantedAuthority> authorities =
                    new ArrayList<>();

            // Rol dentro de la aplicación
            authorities.add(
                    new SimpleGrantedAuthority(
                            "ROLE_" + rol
                    )
            );

            // Permiso global de plataforma
            if (superAdmin) {
                authorities.add(
                        new SimpleGrantedAuthority(
                                "ROLE_SUPER_ADMIN"
                        )
                );
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            authorities
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}