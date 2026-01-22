package com.oxalio.invoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;  // ← AJOUTER

@SpringBootApplication
@EnableRetry  // ← AJOUTER CETTE LIGNE
@EnableConfigurationProperties
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}