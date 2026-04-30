package com.insight_recruit.backend.app.repository;

import com.insight_recruit.backend.app.domain.entity.AuditLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByTenant_Id(UUID tenantId);
    List<AuditLog> findByResourceId(UUID resourceId);
}
