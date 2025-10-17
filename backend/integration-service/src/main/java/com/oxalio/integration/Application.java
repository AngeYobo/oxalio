package com.oxalio.integration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@SpringBootApplication
@RestController
@RequestMapping("/api/v1")
public class Application {
  public static void main(String[] args){ SpringApplication.run(Application.class, args); }

  @PostMapping("/webhooks/dgi-callback")
  public ResponseEntity<Void> callback(){ return ResponseEntity.noContent().build(); }
}
