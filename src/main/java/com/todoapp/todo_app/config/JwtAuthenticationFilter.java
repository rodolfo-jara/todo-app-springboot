package com.todoapp.todo_app.config;

import com.todoapp.todo_app.entity.UsuarioAplicacion;
import com.todoapp.todo_app.repository.UsuarioAplicacionRepository;
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
    private final UsuarioAplicacionRepository usuarioAplicacionRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioAplicacionRepository usuarioAplicacionRepository
    ) {
        this.jwtService = jwtService;
        this.usuarioAplicacionRepository = usuarioAplicacionRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.substring(7);

        if (!jwtService.tokenValido(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extraerEmail(token);
        String rolToken = jwtService.extraerRol(token);
        String appToken = jwtService.extraerApp(token);

        boolean superAdminToken =
                jwtService.extraerSuperAdmin(token);

        String appRequest =
                request.getHeader("X-App-Id");

        if (appRequest == null
                || !appRequest.equals(appToken)) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token no válido para esta aplicación"
            );
            return;
        }

        UsuarioAplicacion acceso =
                usuarioAplicacionRepository
                        .findByUsuarioEmailAndAplicacionCodigo(
                                email,
                                appToken
                        )
                        .orElse(null);

        /*
         * El JWT puede estar correctamente firmado pero haber quedado
         * obsoleto respecto al estado actual de la base de datos.
         *
         * En ese caso NO creamos Authentication.
         */
        if (acceso == null
                || !acceso.isActivo()
                || !acceso.getAplicacion().isActivo()
                || !acceso.getRol().equals(rolToken)
                || acceso.getUsuario().isSuperAdmin() != superAdminToken) {

            filterChain.doFilter(request, response);
            return;
        }

        List<GrantedAuthority> authorities =
                new ArrayList<>();

        authorities.add(
                new SimpleGrantedAuthority(
                        "ROLE_" + acceso.getRol()
                )
        );

        if (acceso.getUsuario().isSuperAdmin()) {
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

        filterChain.doFilter(request, response);
    }
}