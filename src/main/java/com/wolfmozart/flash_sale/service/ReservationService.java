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

}
