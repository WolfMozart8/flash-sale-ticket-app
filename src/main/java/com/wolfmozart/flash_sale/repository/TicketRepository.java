package com.wolfmozart.flash_sale.repository;

import com.wolfmozart.flash_sale.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // @Modifying le dice a Spring: "Ojo, esta consulta va a modificar datos, no es un simple SELECT"
    @Modifying
    @Transactional
    // nativeQuery = true le dice: "No intentes traducir esto, mándalo a Postgres tal cual"
    @Query(value = "CALL confirmar_compra(:ticketId, :usuarioId)", nativeQuery = true)
    void confirmarCompra(@Param("ticketId") Long ticketId, @Param("usuarioId") String usuarioId);
}
