package com.sesame.neobte.Config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        // Registers JavaTimeModule and other discovered modules
        return new ObjectMapper().findAndRegisterModules();
    }
}

