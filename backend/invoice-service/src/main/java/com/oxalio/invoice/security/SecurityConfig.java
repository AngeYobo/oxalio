// src/main/java/com/oxalio/invoice/security/SecurityConfig.java
package com.oxalio.invoice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable()) // Désactive le CSRF pour les tests API
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/api/auth/**",
          "/actuator/**",
          "/swagger-ui/**",
          "/v3/api-docs/**",
          "/h2-console/**"
        ).permitAll()
        .anyRequest().permitAll()   // en mock/dev
      )
      .httpBasic(Customizer.withDefaults())
      .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

    return http.build();
  }
}
