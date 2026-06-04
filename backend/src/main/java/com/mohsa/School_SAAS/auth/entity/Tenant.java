package com.mohsa.School_SAAS.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants")
public class Tenant {

	@Id
	@Column(columnDefinition = "CHAR(36)")
	private UUID id;

	@Column(name = "tenant_code", nullable = false, unique = true, length = 50)
	private String tenantCode;

	@Column(name = "institution_name", nullable = false)
	private String institutionName;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Tenant() {
	}

	public Tenant(String tenantCode, String institutionName) {
		this.tenantCode = tenantCode;
		this.institutionName = institutionName;
	}

	@PrePersist
	void onCreate() {
		if (id == null) {
			id = UUID.randomUUID();
		}
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public String getTenantCode() {
		return tenantCode;
	}

	public String getInstitutionName() {
		return institutionName;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
