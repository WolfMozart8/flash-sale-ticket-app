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

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

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

    @Test
    void deberiaConfirmarVentaYCambiarEstadoEnBaseDeDatos() {
        // 1. PREPARACIÓN: El usuario bloquea el asiento en Redis primero
        reservationService.lockTicket(ticketPrueba.getId(), "Usuario1");

        // 2. ACCIÓN: El usuario confirma la compra (simulando el pago)
        String resultado = reservationService.confirmarVentaFina(ticketPrueba.getId(), "Usuario1");

        // 3. VERIFICACIÓN A: El servicio nos debe devolver el mensaje de éxito
        assertEquals("¡Compra exitosa! Tu ticket está confirmado.", resultado);

        // 4. VERIFICACIÓN B: Fuimos a la base de datos a ver si el PL/pgSQL funcionó
        Ticket ticketActualizado = ticketRepository.findById(ticketPrueba.getId()).get();
        assertEquals("VENDIDO", ticketActualizado.getStatus(), "El estado en PostgreSQL debería ser VENDIDO");

        // 5. VERIFICACIÓN C: Revisamos que Redis haya borrado la reserva para liberar memoria
        Boolean sigueBloqueado = redisTemplate.hasKey("ticket:lock:" + ticketPrueba.getId());
        assertFalse(sigueBloqueado, "La llave de Redis debería haberse borrado tras la compra");
    }

    @Test
    void deberiaDevolverTicketYDejarloDisponible() {

        // Intento... Se debe priorizar solo metodos que se quieren probar
        // Evitar ingresar otros metodos si no son necesarios

//        reservationService.lockTicket(ticketPrueba.getId(), "Usuario1");
//        reservationService.confirmarVentaFina(ticketPrueba.getId(), "Usuario1");
//
//        Ticket ticketComprado = ticketRepository.findById(ticketPrueba.getId()).get();
//
//        assertEquals("VENDIDO", ticketComprado.getStatus(), "El ticket debe iniciar como VENDIDO");
//        String resultado = reservationService.procesarDevolucion(ticketPrueba.getId());
//        assertEquals("Ticket devuelto exitosamente.", resultado);
//        Ticket ticketDevuelto = ticketRepository.findById(ticketPrueba.getId()).get();
//
//        assertEquals("DISPONIBLE", ticketDevuelto.getStatus(), "El ticket debe estar DISPONIBLE despues de devolucion");


        // 1. PREPARACIÓN (Truqueamos el estado directamente en la BD sin usar los otros servicios)
        ticketPrueba.setStatus("VENDIDO");
        ticketRepository.save(ticketPrueba); // Actualizamos el ticket a VENDIDO en Postgres

        // 2. ACCIÓN: Ejecutamos el metodo que realmente queremos probar
        String resultado = reservationService.procesarDevolucion(ticketPrueba.getId());

        // 3. VERIFICACIÓN: Comprobamos que el servicio respondió bien y la DB se actualizó
        assertEquals("Ticket devuelto exitosamente.", resultado);

        Ticket ticketDevuelto = ticketRepository.findById(ticketPrueba.getId()).get();
        assertEquals("DISPONIBLE", ticketDevuelto.getStatus(), "El ticket debe estar DISPONIBLE despues de devolucion");

        // Extra: Validamos que borró la llave de Redis por si acaso
        assertFalse(redisTemplate.hasKey("ticket:lock:" + ticketPrueba.getId()));
    }
}
