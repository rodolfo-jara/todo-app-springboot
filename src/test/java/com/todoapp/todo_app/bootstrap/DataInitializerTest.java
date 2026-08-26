package com.todoapp.todo_app.bootstrap;

import com.todoapp.todo_app.repository.AplicacionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DataInitializerTest {

    @Autowired
    private AplicacionRepository aplicacionRepository;

    @Autowired
    private DataInitializer dataInitializer;

    @Test
    void debeCrearAplicacionesPermitidasSinDuplicarlas() throws Exception {

        aplicacionRepository.deleteAll();

        dataInitializer.run();

        assertTrue(
                aplicacionRepository.findByCodigo("todo-app").isPresent()
        );

        assertTrue(
                aplicacionRepository.findByCodigo("demoapp").isPresent()
        );

        assertEquals(
                2,
                aplicacionRepository.count()
        );

        // Lo ejecutamos una segunda vez.
        dataInitializer.run();

        // Deben seguir existiendo solo 2.
        assertEquals(
                2,
                aplicacionRepository.count()
        );
    }
}