package com.mohsa.School_SAAS.auth.security;

import com.mohsa.School_SAAS.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

	public static final String CLAIM_USER_ID = "uid";
	public static final String CLAIM_TENANT_ID = "tid";
	public static final String CLAIM_TENANT_CODE = "tcode";
	public static final String CLAIM_ROLE = "role";
	public static final String TOKEN_TYPE_ACCESS = "access";
	public static final String TOKEN_TYPE_REFRESH = "refresh";

	private final JwtProperties jwtProperties;
	private final SecretKey secretKey;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;
		this.secretKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
	}

	public String generateAccessToken(AuthenticatedUser user) {
		return buildToken(user, TOKEN_TYPE_ACCESS, jwtProperties.accessTokenExpirationMs());
	}

	public String generateRefreshTokenValue(AuthenticatedUser user) {
		return buildToken(user, TOKEN_TYPE_REFRESH, jwtProperties.refreshTokenExpirationMs());
	}

	public long getAccessTokenExpirationSeconds() {
		return jwtProperties.accessTokenExpirationMs() / 1000;
	}

	public long getRefreshTokenExpirationMs() {
		return jwtProperties.refreshTokenExpirationMs();
	}

	public boolean isAccessToken(String token) {
		return TOKEN_TYPE_ACCESS.equals(parseClaims(token).get("typ", String.class));
	}

	public boolean isRefreshToken(String token) {
		return TOKEN_TYPE_REFRESH.equals(parseClaims(token).get("typ", String.class));
	}

	public UUID extractUserId(String token) {
		return UUID.fromString(parseClaims(token).get(CLAIM_USER_ID, String.class));
	}

	public void validateToken(String token) {
		parseClaims(token);
	}

	private String buildToken(AuthenticatedUser user, String type, long expirationMs) {
		Instant now = Instant.now();
		return Jwts.builder()
				.id(UUID.randomUUID().toString())
				.subject(user.getUsername())
				.claim(CLAIM_USER_ID, user.getId().toString())
				.claim(CLAIM_TENANT_ID, user.getTenantId().toString())
				.claim(CLAIM_TENANT_CODE, user.getTenantCode())
				.claim(CLAIM_ROLE, user.getRoleName())
				.claim("typ", type)
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(expirationMs)))
				.signWith(secretKey)
				.compact();
	}

	private Claims parseClaims(String token) {
		try {
			return Jwts.parser()
					.verifyWith(secretKey)
					.build()
					.parseSignedClaims(token)
					.getPayload();
		}
		catch (JwtException ex) {
			throw new InvalidTokenException("Invalid or expired token", ex);
		}
	}
}
