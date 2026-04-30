package com.insight_recruit.backend.app.repository;

import com.insight_recruit.backend.app.domain.entity.Tenant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findByApiKey(String apiKey);
}
