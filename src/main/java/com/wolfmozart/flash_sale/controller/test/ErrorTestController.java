package com.wolfmozart.flash_sale.controller.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/errors")
public class ErrorTestController {

    @GetMapping("/runtime")
    public String throwRuntime() {
        throw new RuntimeException("Forzando RuntimeException para pruebas");
    }

//    @GetMapping("/notfound")
//    public String throwNotFound() {
//        throw new TicketNotFoundException("Simulando TicketNotFoundException");
//    }

    @GetMapping("/illegal")
    public String throwIllegalArgument() {
        throw new IllegalArgumentException("Argumento inválido en prueba");
    }
}
