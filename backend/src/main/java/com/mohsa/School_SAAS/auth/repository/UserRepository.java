package com.mohsa.School_SAAS.auth.repository;

import com.mohsa.School_SAAS.auth.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

	@Query("""
			SELECT u FROM User u
			JOIN FETCH u.tenant t
			JOIN FETCH u.role r
			WHERE t.id = :tenantId
			  AND u.active = true
			  AND (
			    LOWER(u.username) = LOWER(:login)
			    OR LOWER(u.email) = LOWER(:login)
			    OR u.mobile = :login
			  )
			""")
	Optional<User> findActiveByTenantIdAndLogin(
			@Param("tenantId") UUID tenantId, @Param("login") String login);

	@Query("""
			SELECT u FROM User u
			JOIN FETCH u.tenant
			JOIN FETCH u.role
			WHERE u.id = :id AND u.active = true
			""")
	Optional<User> findByIdAndActiveTrue(@Param("id") UUID id);
}
