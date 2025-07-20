package com.multipagos.p2p_backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // NUEVA IMPORTACIÓN para Java 8 Date/Time
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class P2pBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(P2pBackendApplication.class, args);
    }

    // Configurar ObjectMapper para manejar proxies de Hibernate y tipos de fecha/hora de Java 8
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Hibernate6Module());
        mapper.registerModule(new JavaTimeModule()); // REGISTRAR NUEVO MÓDULO para Java 8 Date/Time
        // Opcional: Deshabilitar FAIL_ON_EMPTY_BEANS si tienes entidades sin propiedades
        // mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return mapper;
    }
}
