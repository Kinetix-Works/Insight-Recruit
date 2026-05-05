package com.insight_recruit.backend.app.repository;

import com.insight_recruit.backend.app.domain.entity.TenantSubscription;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantSubscriptionRepository extends JpaRepository<TenantSubscription, UUID> {}
