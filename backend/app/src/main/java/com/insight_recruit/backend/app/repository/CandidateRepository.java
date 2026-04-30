package com.insight_recruit.backend.app.repository;

import com.insight_recruit.backend.app.domain.entity.Candidate;
import com.insight_recruit.backend.app.domain.enums.ProcessingStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepository extends JpaRepository<Candidate, UUID> {
    List<Candidate> findByTenant_Id(UUID tenantId);
    List<Candidate> findByJob_Id(UUID jobId);
    List<Candidate> findByStatus(ProcessingStatus status);
}
