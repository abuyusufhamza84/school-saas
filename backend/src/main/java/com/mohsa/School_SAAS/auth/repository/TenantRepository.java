package com.mohsa.School_SAAS.auth.repository;

import com.mohsa.School_SAAS.auth.entity.Tenant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

	Optional<Tenant> findByTenantCodeAndActiveTrue(String tenantCode);
}
