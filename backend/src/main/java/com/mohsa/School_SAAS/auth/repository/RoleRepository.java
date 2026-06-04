package com.mohsa.School_SAAS.auth.repository;

import com.mohsa.School_SAAS.auth.entity.Role;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {
}
