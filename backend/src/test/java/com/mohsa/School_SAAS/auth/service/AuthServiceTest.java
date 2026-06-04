package com.mohsa.School_SAAS.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mohsa.School_SAAS.auth.dto.LoginRequest;
import com.mohsa.School_SAAS.auth.dto.TokenResponse;
import com.mohsa.School_SAAS.auth.entity.Role;
import com.mohsa.School_SAAS.auth.entity.Tenant;
import com.mohsa.School_SAAS.auth.entity.User;
import com.mohsa.School_SAAS.auth.exception.InvalidCredentialsException;
import com.mohsa.School_SAAS.auth.repository.RefreshTokenRepository;
import com.mohsa.School_SAAS.auth.repository.TenantRepository;
import com.mohsa.School_SAAS.auth.repository.UserRepository;
import com.mohsa.School_SAAS.auth.security.JwtService;
import com.mohsa.School_SAAS.auth.security.TokenHasher;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private TenantRepository tenantRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	@Mock
	private TokenHasher tokenHasher;

	@InjectMocks
	private AuthService authService;

	private Tenant tenant;
	private User user;

	@BeforeEach
	void setUp() {
		tenant = new Tenant("DEMO", "Demo School");
		Role role = new Role(tenant, "ADMIN", "Administrator");
		user = new User(tenant, role, "admin", "admin@demo.com", null, "$2a$hash", "Admin", "User");
	}

	@Test
	void loginReturnsTokensWhenCredentialsValid() {
		when(tenantRepository.findByTenantCodeAndActiveTrue("DEMO")).thenReturn(Optional.of(tenant));
		when(userRepository.findActiveByTenantIdAndLogin(eq(tenant.getId()), eq("admin@demo.com")))
				.thenReturn(Optional.of(user));
		when(passwordEncoder.matches("secret", user.getPasswordHash())).thenReturn(true);
		when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
		when(jwtService.generateAccessToken(any())).thenReturn("access");
		when(jwtService.generateRefreshTokenValue(any())).thenReturn("refresh");
		when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
		when(jwtService.getRefreshTokenExpirationMs()).thenReturn(604_800_000L);
		when(tokenHasher.hash("refresh")).thenReturn("hashed");

		TokenResponse response = authService.login(new LoginRequest("DEMO", "admin@demo.com", "secret"));

		assertThat(response.accessToken()).isEqualTo("access");
		assertThat(response.refreshToken()).isEqualTo("refresh");
		assertThat(response.username()).isEqualTo("admin");
		verify(refreshTokenRepository).save(any());
	}

	@Test
	void loginFailsWhenPasswordInvalid() {
		when(tenantRepository.findByTenantCodeAndActiveTrue("DEMO")).thenReturn(Optional.of(tenant));
		when(userRepository.findActiveByTenantIdAndLogin(eq(tenant.getId()), eq("admin")))
				.thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong", user.getPasswordHash())).thenReturn(false);

		assertThatThrownBy(() -> authService.login(new LoginRequest("DEMO", "admin", "wrong")))
				.isInstanceOf(InvalidCredentialsException.class);
	}

	@Test
	void loginFailsWhenTenantUnknown() {
		when(tenantRepository.findByTenantCodeAndActiveTrue("UNKNOWN")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.login(new LoginRequest("UNKNOWN", "admin", "secret")))
				.isInstanceOf(InvalidCredentialsException.class);
	}
}
