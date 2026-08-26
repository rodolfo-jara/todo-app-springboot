package com.todoapp.todo_app.bootstrap;

import com.todoapp.todo_app.entity.Aplicacion;
import com.todoapp.todo_app.repository.AplicacionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AplicacionRepository aplicacionRepository;

    public DataInitializer(AplicacionRepository aplicacionRepository) {
        this.aplicacionRepository = aplicacionRepository;
    }

    @Override
    public void run(String... args) {

        crearAplicacionSiNoExiste(
                "todo-app",
                "Todo App"
        );

        crearAplicacionSiNoExiste(
                "demoapp",
                "Demo App"
        );
    }

    private void crearAplicacionSiNoExiste(
            String codigo,
            String nombre
    ) {

        if (aplicacionRepository.findByCodigo(codigo).isEmpty()) {

            Aplicacion aplicacion =
                    new Aplicacion(codigo, nombre);

            aplicacionRepository.save(aplicacion);
        }
    }
}