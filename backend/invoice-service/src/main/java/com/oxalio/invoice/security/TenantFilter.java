package com.oxalio.invoice.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class TenantFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Extraire le tenant depuis le JWT (à implémenter)
        // Pour l'instant, utiliser un header custom
        String tenantId = httpRequest.getHeader("X-Tenant-ID");
        
        if (tenantId != null) {
            TenantContext.setTenantId(Long.parseLong(tenantId));
            log.debug("Tenant context set: {}", tenantId);
        }
        
        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}