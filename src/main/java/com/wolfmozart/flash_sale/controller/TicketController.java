package com.wolfmozart.flash_sale.controller;

import com.wolfmozart.flash_sale.exception.TicketNoEncontradoException;
import com.wolfmozart.flash_sale.model.Ticket;
import com.wolfmozart.flash_sale.repository.TicketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Manejo de tickets")
public class TicketController {

    private final TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/ticket/{id}")
    @Operation(summary = "Obtiene ticket por id", description = "Se debe ingresar un id de un ticket")
    public ResponseEntity<Ticket> obtenerTicketPorId(@PathVariable Long id) {
//        Optional<Ticket> ticket = ticketRepository.findById(id);

//        if (ticket.isEmpty()) {
//            throw new TicketNoEncontradoException("El ticket con id " + id + " no existe");
//        }
//
//        return ResponseEntity.ok().body(ticket);

        return ticketRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new TicketNoEncontradoException("El ticket con id " + id + " no existe"));
    }

    @GetMapping("/all")
    @Cacheable(value = "ListaTickets")
    @Operation(summary = "Obtiene todos los tickets", description = "Obtiene todos los tickets disponibles")
    public List<Ticket> obtenerTodosLosTickets() {
        System.out.println("⚠️ ATENCIÓN: Fui a buscar los datos a PostgreSQL ⚠️");
        return ticketRepository.findAll();
    }

    @GetMapping("/mock")
    @Operation(summary = "Crea datos de prueba", description = "Crea datos MOCK para poblar base de datos inicial")
    public String crearMock() {
        Ticket ticket1 = new Ticket("Concierto Test", "VENDIDO", new BigDecimal(1000));
        Ticket ticket2 = new Ticket("Festival Primavera", "DISPONIBLE", new BigDecimal(500));
        Ticket ticket3 = new Ticket("Obra de Teatro", "RESERVADO", new BigDecimal(750));

        ticketRepository.save(ticket1);
        ticketRepository.save(ticket2);
        ticketRepository.save(ticket3);



        return "Se han creado 3 tickets";
    }

}
