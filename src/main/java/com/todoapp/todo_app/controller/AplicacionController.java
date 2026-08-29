package com.todoapp.todo_app.controller;

import com.todoapp.todo_app.dto.AplicacionEstadoRequest;
import com.todoapp.todo_app.dto.AplicacionRequest;
import com.todoapp.todo_app.dto.AplicacionResponse;
import com.todoapp.todo_app.dto.AplicacionUpdateRequest;
import com.todoapp.todo_app.service.AplicacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aplicaciones")
public class AplicacionController {

    private final AplicacionService aplicacionService;

    public AplicacionController(AplicacionService aplicacionService) {
        this.aplicacionService = aplicacionService;
    }

    @PostMapping
    public ResponseEntity<AplicacionResponse> crear(
            @Valid @RequestBody AplicacionRequest request
    ) {
        AplicacionResponse creada = aplicacionService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creada);
    }

    @GetMapping
    public ResponseEntity<List<AplicacionResponse>> listar(
            @RequestHeader("X-App-Id") String appId
    ) {
        return ResponseEntity.ok(aplicacionService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AplicacionResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                aplicacionService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AplicacionResponse> editar(
            @PathVariable Long id,
            @Valid @RequestBody AplicacionUpdateRequest request
    ) {
        return ResponseEntity.ok(
                aplicacionService.editar(id, request)
        );
    }
    @PatchMapping("/{id}/estado")
    public ResponseEntity<AplicacionResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody AplicacionEstadoRequest request
    ) {
        return ResponseEntity.ok(
                aplicacionService.cambiarEstado(id, request)
        );
    }
}