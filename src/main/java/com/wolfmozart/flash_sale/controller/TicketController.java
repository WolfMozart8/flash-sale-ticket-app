package com.wolfmozart.flash_sale.controller;

import com.wolfmozart.flash_sale.model.Ticket;
import com.wolfmozart.flash_sale.repository.TicketRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private TicketRepository ticketRepository;

    public TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping("/all")
    @Cacheable(value = "ListaTickets")
    public List<Ticket> obtenerTodosLosTickets() {
        System.out.println("⚠️ ATENCIÓN: Fui a buscar los datos a PostgreSQL ⚠️");
        return ticketRepository.findAll();
    }

    @GetMapping("/mock")
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
