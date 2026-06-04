package com.mohsa.School_SAAS.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mohsa.School_SAAS.auth.config.JwtProperties;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

	private JwtService jwtService;
	private AuthenticatedUser user;

	@BeforeEach
	void setUp() {
		JwtProperties properties = new JwtProperties(
				"test-secret-key-at-least-256-bits-long-for-hmac-sha256-signing",
				900_000,
				604_800_000);
		jwtService = new JwtService(properties);
		user = new AuthenticatedUser(
				UUID.randomUUID(),
				UUID.randomUUID(),
				"DEMO",
				"admin",
				"hash",
				"ADMIN",
				true);
	}

	@Test
	void generatesAccessAndRefreshTokensWithDistinctTypes() {
		String access = jwtService.generateAccessToken(user);
		String refresh = jwtService.generateRefreshTokenValue(user);

		assertThat(jwtService.isAccessToken(access)).isTrue();
		assertThat(jwtService.isRefreshToken(refresh)).isTrue();
		assertThat(jwtService.extractUserId(access)).isEqualTo(user.getId());
	}

	@Test
	void rejectsInvalidToken() {
		assertThatThrownBy(() -> jwtService.validateToken("not.a.valid.jwt"))
				.isInstanceOf(InvalidTokenException.class);
	}
}
