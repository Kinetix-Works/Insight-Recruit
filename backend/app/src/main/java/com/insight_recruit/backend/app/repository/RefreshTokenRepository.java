package com.insight_recruit.backend.app.repository;

import com.insight_recruit.backend.app.domain.entity.RefreshToken;
import com.insight_recruit.backend.app.domain.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenId(String tokenId);
    void deleteByUser(User user);
}
