package com.wolfmozart.flash_sale.controller;

import com.wolfmozart.flash_sale.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/lock")
    public ResponseEntity<String> lockTicket(@RequestParam Long ticketId, @RequestParam String userId) {
        boolean success = reservationService.lockTicket(ticketId, userId);

        if (success) {
            return ResponseEntity.ok("Ticket reservado exitosamente por 5 minutos.");
        } else {
            return ResponseEntity.badRequest().body("Error: Ticket no disponible o ya reservado por alguien más.");
        }
    }
}
