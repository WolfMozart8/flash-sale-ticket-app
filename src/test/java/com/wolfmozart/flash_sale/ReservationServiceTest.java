package com.wolfmozart.flash_sale;

import com.wolfmozart.flash_sale.model.Ticket;
import com.wolfmozart.flash_sale.repository.TicketRepository;
import com.wolfmozart.flash_sale.service.ReservationService;
import org.hibernate.annotations.Array;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers // 1. Activa la magia de Testcontainers
public class ReservationServiceTest {

    // 2. Le decimos: "Levanta un Redis versión 7 de Alpine (ligero) en el puerto 6379"
    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    //! Error en test, se agregan anotaciones de arriba
    // 3. Como el contenedor elige un puerto aleatorio en tu PC para no chocar con nada,
    // le inyectamos esa configuración a Spring Boot en tiempo real.
//    @DynamicPropertySource
//    static void redisProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.data.redis.host", redisContainer::getHost);
//        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
//    }

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private TicketRepository ticketRepository;

    private Ticket ticketPrueba;

    // Esto se ejecuta ANTES de cada prueba para preparar el terreno
    @BeforeEach
    void setup() {
        ticketRepository.deleteAll();
        ticketPrueba = new Ticket("Concierto Test", "DISPONIBLE", new BigDecimal("1000"));
        ticketRepository.save(ticketPrueba);
    }

    @Test
    void deberiaBloquearElTicketSiEstaDisponible() {
        // Acción: El Usuario 1 intenta bloquear el ticket
        boolean resultado = reservationService.lockTicket(ticketPrueba.getId(), "Usuario1");

        // Verificación: Debería ser exitoso (true)
        assertTrue(resultado, "El ticket debería haberse bloqueado exitosamente");
    }

    @Test
    void noDeberiaPermitirDobleBloqueo() {
        // Acción: El Usuario 1 bloquea el ticket primero
        reservationService.lockTicket(ticketPrueba.getId(), "Usuario1");

        // Acción: El Usuario 2 intenta bloquear el MISMO ticket inmediatamente después
        boolean resultadoSegundoIntento = reservationService.lockTicket(ticketPrueba.getId(), "Usuario2");

        // Verificación: El segundo intento debe fallar (false)
        assertFalse(resultadoSegundoIntento, "El segundo usuario no debería poder bloquear un ticket ya reservado");
    }
}
