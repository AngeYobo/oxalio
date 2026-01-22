package com.oxalio.invoice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("dev")
public class SecurityConfigDev {

  @Bean
  @Order(0)
  SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
    http
      .securityMatcher("/**")
      .csrf(csrf -> csrf.disable())
      .httpBasic(basic -> basic.disable())
      .formLogin(form -> form.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/api/v1/**",
          "/v3/api-docs/**",
          "/swagger-ui/**",
          "/swagger-ui.html",
          "/actuator/**"
        ).permitAll()
        .anyRequest().permitAll()
      );

    return http.build();
  }
}
