package com.sesame.neobte;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class NeobteApplication {

    public static void main(String[] args) {
        SpringApplication.run(NeobteApplication.class, args);
    }

}
