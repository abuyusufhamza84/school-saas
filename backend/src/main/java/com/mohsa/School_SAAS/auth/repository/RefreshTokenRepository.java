package com.mohsa.School_SAAS.auth.repository;

import com.mohsa.School_SAAS.auth.entity.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

	@Query("""
			SELECT rt FROM RefreshToken rt
			JOIN FETCH rt.user u
			JOIN FETCH u.tenant
			JOIN FETCH u.role
			WHERE rt.tokenHash = :tokenHash
			  AND rt.revoked = false
			""")
	Optional<RefreshToken> findValidByTokenHash(@Param("tokenHash") String tokenHash);

	@Modifying
	@Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.user.id = :userId AND rt.revoked = false")
	void revokeAllByUserId(@Param("userId") UUID userId);
}
