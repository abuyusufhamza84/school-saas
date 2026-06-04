package com.mohsa.School_SAAS.auth.dto;

import java.util.UUID;

public record TokenResponse(
		String accessToken,
		String refreshToken,
		String tokenType,
		long expiresInSeconds,
		UUID userId,
		String username,
		String role) {
}
