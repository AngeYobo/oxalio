package com.oxalio.invoice.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@Profile("mock")
@RestController
@RequestMapping("/api/auth")
public class AuthControllerMock {

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
    String email = String.valueOf(body.getOrDefault("email", "demo@oxalio.local"));

    Map<String, Object> user = Map.of(
      "id", 1,
      "email", email,
      "name", "Demo User",
      "role", "admin",
      "companyName", "OXALIO SARL",
      "companyNcc", "2505842N",
      "createdAt", OffsetDateTime.now().toString()
    );

    Map<String, Object> resp = Map.of(
      "user", user,
      "token", "MOCK_JWT_TOKEN",
      "expiresIn", 3600
    );

    return ResponseEntity.ok(resp);
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
    // En mock: on renvoie la même structure qu’un login
    return login(body);
  }
}
