package com.oxalio.gateway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@SpringBootApplication
@RestController
@RequestMapping("/api")
public class Application {
  public static void main(String[] args){ SpringApplication.run(Application.class, args); }

  @GetMapping("/v1/health") public ResponseEntity<String> health(){ return ResponseEntity.ok("OK"); }
  @GetMapping("/v1/readiness") public ResponseEntity<String> ready(){ return ResponseEntity.ok("READY"); }
}
