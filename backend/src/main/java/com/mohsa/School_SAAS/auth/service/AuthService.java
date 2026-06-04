package com.mohsa.School_SAAS.auth.service;

import com.mohsa.School_SAAS.auth.dto.LoginRequest;
import com.mohsa.School_SAAS.auth.dto.RefreshTokenRequest;
import com.mohsa.School_SAAS.auth.dto.TokenResponse;
import com.mohsa.School_SAAS.auth.entity.RefreshToken;
import com.mohsa.School_SAAS.auth.entity.Tenant;
import com.mohsa.School_SAAS.auth.entity.User;
import com.mohsa.School_SAAS.auth.exception.InvalidCredentialsException;
import com.mohsa.School_SAAS.auth.repository.RefreshTokenRepository;
import com.mohsa.School_SAAS.auth.repository.TenantRepository;
import com.mohsa.School_SAAS.auth.repository.UserRepository;
import com.mohsa.School_SAAS.auth.security.AuthUserDetailsService;
import com.mohsa.School_SAAS.auth.security.AuthenticatedUser;
import com.mohsa.School_SAAS.auth.security.InvalidTokenException;
import com.mohsa.School_SAAS.auth.security.JwtService;
import com.mohsa.School_SAAS.auth.security.TokenHasher;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final TenantRepository tenantRepository;
	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final TokenHasher tokenHasher;

	public AuthService(
			TenantRepository tenantRepository,
			UserRepository userRepository,
			RefreshTokenRepository refreshTokenRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			TokenHasher tokenHasher) {
		this.tenantRepository = tenantRepository;
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.tokenHasher = tokenHasher;
	}

	@Transactional
	public TokenResponse login(LoginRequest request) {
		Tenant tenant = tenantRepository
				.findByTenantCodeAndActiveTrue(request.tenantCode())
				.orElseThrow(InvalidCredentialsException::new);

		User user = userRepository
				.findActiveByTenantIdAndLogin(tenant.getId(), request.login().trim())
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		user.recordLogin();
		return issueTokens(AuthUserDetailsService.toAuthenticatedUser(user));
	}

	@Transactional
	public TokenResponse refresh(RefreshTokenRequest request) {
		String rawRefreshToken = request.refreshToken();
		jwtService.validateToken(rawRefreshToken);
		if (!jwtService.isRefreshToken(rawRefreshToken)) {
			throw new InvalidTokenException("Token is not a refresh token");
		}

		String tokenHash = tokenHasher.hash(rawRefreshToken);
		RefreshToken stored = refreshTokenRepository
				.findValidByTokenHash(tokenHash)
				.orElseThrow(() -> new InvalidTokenException("Refresh token not found or revoked"));

		if (stored.isExpired()) {
			stored.revoke();
			throw new InvalidTokenException("Refresh token expired");
		}

		stored.revoke();
		AuthenticatedUser user = AuthUserDetailsService.toAuthenticatedUser(stored.getUser());
		return issueTokens(user);
	}

	@Transactional
	public void logout(String rawRefreshToken) {
		if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
			return;
		}
		try {
			jwtService.validateToken(rawRefreshToken);
			if (!jwtService.isRefreshToken(rawRefreshToken)) {
				return;
			}
			refreshTokenRepository
					.findValidByTokenHash(tokenHasher.hash(rawRefreshToken))
					.ifPresent(token -> {
						token.revoke();
						refreshTokenRepository.save(token);
					});
		}
		catch (InvalidTokenException ignored) {
			// Idempotent logout
		}
	}

	private TokenResponse issueTokens(AuthenticatedUser user) {
		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.generateRefreshTokenValue(user);
		persistRefreshToken(user, refreshToken);
		return new TokenResponse(
				accessToken,
				refreshToken,
				"Bearer",
				jwtService.getAccessTokenExpirationSeconds(),
				user.getId(),
				user.getUsername(),
				user.getRoleName());
	}

	private void persistRefreshToken(AuthenticatedUser user, String rawRefreshToken) {
		User entity = userRepository.findById(user.getId()).orElseThrow();
		String tokenHash = tokenHasher.hash(rawRefreshToken);
		Instant expiresAt = Instant.now().plusMillis(jwtService.getRefreshTokenExpirationMs());
		refreshTokenRepository.save(new RefreshToken(entity, tokenHash, expiresAt));
	}

}
