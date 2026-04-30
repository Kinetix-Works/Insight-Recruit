package com.insight_recruit.backend.app.repository;

import com.insight_recruit.backend.app.domain.entity.Job;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, UUID> {
    List<Job> findByTenant_Id(UUID tenantId);
}
