package com.insight_recruit.backend.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insight_recruit.backend.app.auth.AuthenticatedUser;
import com.insight_recruit.backend.app.domain.enums.SubscriptionStatus;
import com.insight_recruit.backend.app.repository.TenantSubscriptionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SubscriptionEnforcementFilter extends OncePerRequestFilter {

    private final TenantSubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SubscriptionEnforcementFilter(TenantSubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (isBillingEndpoint(request.getRequestURI()) || !isCoreProcessingEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            filterChain.doFilter(request, response);
            return;
        }

        SubscriptionStatus status = subscriptionRepository.findById(principal.tenantId())
            .map(sub -> sub.getStatus())
            .orElse(SubscriptionStatus.ACTIVE);

        if (status == SubscriptionStatus.PAST_DUE) {
            writeError(response, HttpStatus.PAYMENT_REQUIRED, "Subscription is past due");
            return;
        }

        if (status == SubscriptionStatus.CANCELED) {
            writeError(response, HttpStatus.FORBIDDEN, "Subscription is canceled");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isCoreProcessingEndpoint(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
            && request.getRequestURI().startsWith("/api/v1/candidates/upload");
    }

    private boolean isBillingEndpoint(String uri) {
        return uri.startsWith("/api/v1/billing");
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), Map.of(
            "timestamp", LocalDateTime.now().toString(),
            "status", status.value(),
            "error", status.getReasonPhrase(),
            "message", message
        ));
    }
}
