package com.mohsa.School_SAAS.auth.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedUser implements UserDetails {

	private final UUID id;
	private final UUID tenantId;
	private final String tenantCode;
	private final String username;
	private final String passwordHash;
	private final String roleName;
	private final boolean active;

	public AuthenticatedUser(
			UUID id,
			UUID tenantId,
			String tenantCode,
			String username,
			String passwordHash,
			String roleName,
			boolean active) {
		this.id = id;
		this.tenantId = tenantId;
		this.tenantCode = tenantCode;
		this.username = username;
		this.passwordHash = passwordHash;
		this.roleName = roleName;
		this.active = active;
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public String getTenantCode() {
		return tenantCode;
	}

	public String getRoleName() {
		return roleName;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase()));
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return active;
	}
}
