package com.todoapp.todo_app.service;

import com.todoapp.todo_app.dto.AplicacionEstadoRequest;
import com.todoapp.todo_app.dto.AplicacionRequest;
import com.todoapp.todo_app.dto.AplicacionResponse;
import com.todoapp.todo_app.dto.AplicacionUpdateRequest;
import com.todoapp.todo_app.entity.Aplicacion;
import com.todoapp.todo_app.repository.AplicacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
public class AplicacionService {

    private final AplicacionRepository aplicacionRepository;
    private final RefreshTokenService refreshTokenService;

    public AplicacionService(
            AplicacionRepository aplicacionRepository,
            RefreshTokenService refreshTokenService
    ) {
        this.aplicacionRepository = aplicacionRepository;
        this.refreshTokenService = refreshTokenService;
    }

    public AplicacionResponse crear(AplicacionRequest request) {

        String codigoNormalizado = request.getCodigo()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (aplicacionRepository.findByCodigo(codigoNormalizado).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una aplicación con ese código"
            );
        }

        Aplicacion aplicacion = new Aplicacion(
                codigoNormalizado,
                request.getNombre()
        );

        Aplicacion guardada = aplicacionRepository.save(aplicacion);

        return convertirAResponse(guardada);
    }

    public List<AplicacionResponse> listar() {
        return aplicacionRepository.findAll()
                .stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public AplicacionResponse buscarPorId(Long id) {

        Aplicacion aplicacion = aplicacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Aplicación no encontrada"
                ));

        return convertirAResponse(aplicacion);
    }

    public AplicacionResponse editar(
            Long id,
            AplicacionUpdateRequest request
    ) {

        Aplicacion aplicacion = aplicacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Aplicación no encontrada"
                ));

        aplicacion.setNombre(request.getNombre());

        Aplicacion guardada = aplicacionRepository.save(aplicacion);

        return convertirAResponse(guardada);
    }

    @Transactional
    public AplicacionResponse cambiarEstado(
            Long id,
            AplicacionEstadoRequest request
    ) {

        Aplicacion aplicacion = aplicacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Aplicación no encontrada"
                ));

        if (!request.getActivo()
                && "auth-admin".equals(aplicacion.getCodigo())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La aplicación auth-admin no puede desactivarse"
            );
        }

        boolean seEstaDesactivando =
                aplicacion.isActivo() && !request.getActivo();

        aplicacion.setActivo(request.getActivo());

        Aplicacion guardada = aplicacionRepository.save(aplicacion);

        if (seEstaDesactivando) {
            refreshTokenService.revocarTodosPorAplicacion(guardada);
        }

        return convertirAResponse(guardada);
    }

    private AplicacionResponse convertirAResponse(Aplicacion aplicacion) {
        return new AplicacionResponse(
                aplicacion.getId(),
                aplicacion.getCodigo(),
                aplicacion.getNombre(),
                aplicacion.isActivo()
        );
    }
}