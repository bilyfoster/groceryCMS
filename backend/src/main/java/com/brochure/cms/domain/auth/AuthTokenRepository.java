package com.brochure.cms.domain.auth;

import com.brochure.cms.enums.AuthTokenPurpose;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthTokenRepository extends JpaRepository<AuthToken, UUID> {

    @Query(
            value = "SELECT * FROM auth_tokens WHERE token_hash = :tokenHash AND purpose = :purpose "
                    + "AND used_at IS NULL LIMIT 1",
            nativeQuery = true)
    Optional<AuthToken> findActiveByTokenHashAndPurpose(
            @Param("tokenHash") String tokenHash, @Param("purpose") String purpose);

    @Modifying(clearAutomatically = true)
    @Query(
            value = "UPDATE auth_tokens SET used_at = CURRENT_TIMESTAMP WHERE user_id = :userId "
                    + "AND purpose = :purpose AND used_at IS NULL",
            nativeQuery = true)
    void invalidatePurposeTokens(@Param("userId") UUID userId, @Param("purpose") String purpose);
}
