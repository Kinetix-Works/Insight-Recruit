package com.insight_recruit.backend.app.repository;

import com.insight_recruit.backend.app.domain.entity.Feedback;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    List<Feedback> findByTenant_Id(UUID tenantId);
    List<Feedback> findByCandidate_Id(UUID candidateId);
}
