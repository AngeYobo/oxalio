package com.oxalio.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(
                    "/api/v1/health",
                    "/api/v1/readiness",
                    "/api/v1/auth/**",
                    "/actuator/**"
                ).permitAll()
                .anyExchange().authenticated()
            )
            // ❌ PAS d'addFilterBefore/At/AfTER ici (on évite SecurityWebFiltersOrder)
            .build();
    }
}
