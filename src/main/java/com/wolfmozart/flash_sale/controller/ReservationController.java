package com.wolfmozart.flash_sale.controller;

import com.wolfmozart.flash_sale.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservaciones", description = "Manejo de reservaciones")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/lock")
    @Operation(summary = "Un usuario reserva un ticket por tiempo limitado", description = "El ticket reservado es solo válido para un usuario durante el momento de lock")
    public ResponseEntity<String> lockTicket(@RequestParam Long ticketId, @RequestParam String userId) {
        boolean success = reservationService.lockTicket(ticketId, userId);

        if (success) {
            return ResponseEntity.ok("Ticket reservado exitosamente por 5 minutos.");
        } else {
            return ResponseEntity.badRequest().body("Error: Ticket no disponible o ya reservado por alguien más.");
        }
    }

    @PostMapping("/confirm")
    @Operation(summary = "Confirma compra de ticket", description = "Confirma Ticket comprado")
    public ResponseEntity<String> confirmTicket(@RequestParam Long ticketId, @RequestParam String userId) {
        String result = reservationService.confirmarVentaFina(ticketId, userId);

        if (result.contains("exitosa")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
