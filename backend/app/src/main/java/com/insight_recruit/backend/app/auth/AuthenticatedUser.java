package com.insight_recruit.backend.app.auth;

import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;

public record AuthenticatedUser(
    UUID userId,
    UUID tenantId,
    String role,
    String subscriptionTier,
    Collection<? extends GrantedAuthority> authorities
) {}
