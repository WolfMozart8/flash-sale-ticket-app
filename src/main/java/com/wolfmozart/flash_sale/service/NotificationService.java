package com.wolfmozart.flash_sale.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    // @KafkaListener es la magia pura. Le dice a Spring:
    // "Mantén este método escuchando este tópico en todo momento"
    @KafkaListener(topics = "ventas-confirmadas-topic", groupId = "flashsale-group")
    public void enviarCorreoDeConfirmacion(String mensaje) {

        System.out.println("\n=======================================================");
        System.out.println("📧 [KAFKA CONSUMER RECIBIÓ UN MENSAJE]");
        System.out.println("Contenido: " + mensaje);
        System.out.println("Simulando: Generando PDF con código QR...");

        try {
            // Simulamos que generar el PDF y enviar el mail toma 2 segundos.
            // Gracias a Kafka, el usuario NO está esperando estos 2 segundos en su navegador.
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("✅ Correo enviado con éxito al usuario.");
        System.out.println("=======================================================\n");
    }

}
