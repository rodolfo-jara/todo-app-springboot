package com.todoapp.todo_app.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class LoginAttemptService {
    private static final int MAX_INTENTOS = 5;
    private static final Duration BLOQUEO = Duration.ofMinutes(15);

    private record Estado(int intentos, Instant bloqueadoHasta) {}

    private final ConcurrentHashMap<String, Estado> intentosPorEmail = new ConcurrentHashMap<>();

    public boolean estaBloqueado(String email) {
        Estado estado = intentosPorEmail.get(clave(email));
        return estado != null
                && estado.bloqueadoHasta() != null
                && Instant.now().isBefore(estado.bloqueadoHasta());
    }

    public void registrarFallo(String email) {
        intentosPorEmail.compute(clave(email), (k, actual) -> {
            int intentos = (actual == null ? 0 : actual.intentos()) + 1;
            Instant bloqueadoHasta = intentos >= MAX_INTENTOS
                    ? Instant.now().plus(BLOQUEO)
                    : null;
            return new Estado(intentos, bloqueadoHasta);
        });
    }

    public void registrarExito(String email) {
        intentosPorEmail.remove(clave(email));
    }

    private String clave(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
