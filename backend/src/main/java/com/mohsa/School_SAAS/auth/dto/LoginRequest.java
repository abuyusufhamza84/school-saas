package com.mohsa.School_SAAS.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank String tenantCode,
		@NotBlank String login,
		@NotBlank String password) {
}
