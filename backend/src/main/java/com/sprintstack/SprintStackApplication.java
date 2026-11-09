package com.sprintstack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SprintStackApplication {
    public static void main(String[] args) {
        SpringApplication.run(SprintStackApplication.class, args);
    }
}
