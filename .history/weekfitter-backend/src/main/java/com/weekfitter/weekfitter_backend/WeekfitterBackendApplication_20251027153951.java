package com.weekfitter.weekfitter_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WeekfitterBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeekfitterBackendApplication.class, args);
    }
}
