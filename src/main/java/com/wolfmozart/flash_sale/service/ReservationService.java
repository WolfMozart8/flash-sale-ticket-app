package com.wolfmozart.flash_sale.service;

import com.wolfmozart.flash_sale.model.Ticket;
import com.wolfmozart.flash_sale.repository.TicketRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ReservationService {

    // Spring Boot inyecta esto automáticamente gracias a la dependencia de Redis
    private final StringRedisTemplate redisTemplate;
    private final TicketRepository ticketRepository;

    public ReservationService(StringRedisTemplate redisTemplate, TicketRepository ticketRepository) {
        this.redisTemplate = redisTemplate;
        this.ticketRepository = ticketRepository;
    }

    public boolean lockTicket(Long ticketId, String userId) {

        // 1. Verificar si el ticket existe en PostgreSQL y si está disponible
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket == null || !"DISPONIBLE".equals(ticket.getStatus())) {
            return false;
        }

        // 2. Intentar bloquear en Redis
        String redisKey = "ticket:lock:" + ticketId;

        // EXPLICACIÓN CLAVE: setIfAbsent es una operación ATÓMICA.
        // Si 1000 usuarios ejecutan esto al mismo tiempo, Redis garantiza que solo UNO
        // recibirá "true". Le damos 5 minutos de vida (Time To Live).

        Boolean isLocked = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, userId, Duration.ofMinutes(5));

        return Boolean.TRUE.equals(isLocked);
    }

    public String confirmarVentaFina(Long ticketId, String userId) {
        String redisKey = "ticket:lock:" + ticketId;

        // 1. Verificamos que el usuario que intenta comprar sea el mismo que reservó en Redis
        String usuarioQueReservo = redisTemplate.opsForValue().get(redisKey);

        if (usuarioQueReservo == null || !usuarioQueReservo.equals(userId)) {
            return "Error: No tienes una reserva activa o ya expiró.";
        }

        try {
            // 2. Llamamos al Procedimiento Almacenado de la base de datos
            ticketRepository.confirmarCompra(ticketId, userId);

            // 3. Si Postgres no tiró error, la compra fue un éxito.
            // Borramos la reserva de Redis porque ya no la necesitamos
            redisTemplate.delete(redisKey);

            return "¡Compra exitosa! Tu ticket está confirmado.";
        } catch (Exception e) {
            // Si el procedimiento almacenado falla (ej. el ticket no estaba DISPONIBLE),
            // capturamos el error aquí.
            return "Error al confirmar la compra en la base de datos.";
        }
    }

    public String procesarDevolucion(Long ticketId) {

        // No necesario comprobar, ya que eso se hace en POSTGRES
//        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
//
//        if (ticket == null) {
//            return "No se encontró ticket con ese ID";
//        } else if (ticket.getStatus().equals("DISPONIBLE")) {
//            return "El ticket debe de estar vendido para poder deolverse";
//        }

        String redisKey = "ticket:lock:" + ticketId;

        try {
            ticketRepository.devolverTicket(ticketId);
            redisTemplate.delete(redisKey);

            return "Ticket devuelto exitosamente.";
        } catch (Exception e) {
            return "Error al confirmar la devolución del ticket en la base de datos";
        }

    }

}
