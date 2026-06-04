package com.mohsa.School_SAAS.auth.security;

import java.util.Collections;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

	private final String jwt;
	private final UserDetails principal;

	public JwtAuthenticationToken(String jwt) {
		super(Collections.emptyList());
		this.jwt = jwt;
		this.principal = null;
		setAuthenticated(false);
	}

	public JwtAuthenticationToken(UserDetails principal, String jwt) {
		super(principal.getAuthorities());
		this.principal = principal;
		this.jwt = jwt;
		setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return jwt;
	}

	@Override
	public Object getPrincipal() {
		return principal;
	}
}
