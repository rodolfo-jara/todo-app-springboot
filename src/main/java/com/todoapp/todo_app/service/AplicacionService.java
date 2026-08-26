package com.todoapp.todo_app.service;

import com.todoapp.todo_app.dto.AplicacionRequest;
import com.todoapp.todo_app.dto.AplicacionResponse;
import com.todoapp.todo_app.entity.Aplicacion;
import com.todoapp.todo_app.repository.AplicacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AplicacionService {

    private final AplicacionRepository aplicacionRepository;

    public AplicacionService(AplicacionRepository aplicacionRepository) {
        this.aplicacionRepository = aplicacionRepository;
    }

    public AplicacionResponse crear(AplicacionRequest request) {

        if (aplicacionRepository.findByCodigo(request.getCodigo()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe una aplicación con ese código"
            );
        }

        Aplicacion aplicacion = new Aplicacion(
                request.getCodigo(),
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

    private AplicacionResponse convertirAResponse(Aplicacion aplicacion) {
        return new AplicacionResponse(
                aplicacion.getId(),
                aplicacion.getCodigo(),
                aplicacion.getNombre(),
                aplicacion.isActivo()
        );
    }
}