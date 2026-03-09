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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.beans.IntrospectionException;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers 
public class ReservationServiceTest {


    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    // Le decimos: "Levanta un Redis versión 7 de Alpine (ligero) en el puerto 6379"
    @Container
    @ServiceConnection(name = "redis")
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Container
    @ServiceConnection
    static org.testcontainers.kafka.KafkaContainer kafkaContainer =
            new org.testcontainers.kafka.KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

    // "hack" para usar version de convluentinc no funcionó en test
//    static org.testcontainers.kafka.KafkaContainer kafkaContainer =
//            new org.testcontainers.kafka.KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.4")
//                    .asCompatibleSubstituteFor("apache/kafka")
//            );



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

    @Test
    void deberiaSoportarConcurrenciaYPermitirSoloUnBloqueo() throws InterruptedException {
        int numeroDeAtacantes = 50;

        // Creamos 50 "trabajadores" (hilos) simultáneos
        ExecutorService executor = Executors.newFixedThreadPool(numeroDeAtacantes);

        // CountDownLatch funciona como el semáforo de una carrera de autos.
        // Lo iniciamos en 1. Todos los hilos esperarán a que llegue a 0 para arrancar juntos.
        CountDownLatch semaforoDePartida = new CountDownLatch(1);

        // Este otro semáforo es para que el Test espere a que los 50 terminen antes de revisar los resultados
        CountDownLatch todosTerminaron = new CountDownLatch(numeroDeAtacantes);

        // Variables seguras para contar en entornos multi-hilo
        AtomicInteger comprasExitosas = new AtomicInteger(0);
        AtomicInteger comprasFallidas = new AtomicInteger(0);

        // Preparamos a los 50 corredores en la línea de partida
        for (int i = 0; i < numeroDeAtacantes; i++) {
            String userId = "UsuarioHacker-" + i;

            executor.submit(() -> {
                try {
                    // El hilo se queda en pausa aquí, esperando el disparo de salida
                    semaforoDePartida.await();

                    // ¡Arrancan! Intentan bloquear el ticket en Redis
                    boolean bloqueado = reservationService.lockTicket(ticketPrueba.getId(), userId);

                    if (bloqueado) {
                        comprasExitosas.incrementAndGet();
                    } else {
                        comprasFallidas.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    // Avisamos que este hilo terminó su trabajo
                    todosTerminaron.countDown();
                }
            });
        }

        // 3... 2... 1... ¡PUM! Bajamos el semáforo a 0.
        // Los 50 hilos atacan a Redis en el mismo milisegundo exacto.
        semaforoDePartida.countDown();

        // Obligamos al test a esperar que se asiente el polvo y todos terminen
        todosTerminaron.await();
        executor.shutdown();

        // LA PRUEBA DEFINITIVA:
        // Solo 1 debió lograrlo, 49 debieron rebotar contra el setIfAbsent de Redis.
        assertEquals(1, comprasExitosas.get(), "Solo 1 usuario debió lograr bloquear el ticket");
        assertEquals(49, comprasFallidas.get(), "49 usuarios debieron ser rechazados por Redis");
    }
}
