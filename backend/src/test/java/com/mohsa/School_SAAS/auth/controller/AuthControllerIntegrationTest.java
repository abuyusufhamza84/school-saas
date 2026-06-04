package com.mohsa.School_SAAS.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.mohsa.School_SAAS.auth.entity.Role;
import com.mohsa.School_SAAS.auth.entity.Tenant;
import com.mohsa.School_SAAS.auth.entity.User;
import com.mohsa.School_SAAS.auth.repository.RefreshTokenRepository;
import com.mohsa.School_SAAS.auth.repository.RoleRepository;
import com.mohsa.School_SAAS.auth.repository.TenantRepository;
import com.mohsa.School_SAAS.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class AuthControllerIntegrationTest {

	private static final String DATABASE_NAME = "auth-it-" + java.util.UUID.randomUUID();

	@DynamicPropertySource
	static void configureDataSource(DynamicPropertyRegistry registry) {
		registry.add(
				"spring.datasource.url",
				() -> "jdbc:h2:mem:" + DATABASE_NAME + ";MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RefreshTokenRepository refreshTokenRepository;

	@Autowired
	private TenantRepository tenantRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private Tenant tenant;

	@BeforeEach
	void seedUser() {
		refreshTokenRepository.deleteAll();
		userRepository.deleteAll();
		roleRepository.deleteAll();
		tenantRepository.deleteAll();

		tenant = tenantRepository.save(new Tenant("DEMO", "Demo School"));
		Role role = roleRepository.save(new Role(tenant, "ADMIN", "Administrator"));
		userRepository.save(new User(
				tenant,
				role,
				"admin",
				"admin@demo.com",
				"9000000000",
				passwordEncoder.encode("Password1!"),
				"Admin",
				"User"));
	}

	@Test
	void loginRefreshAndLogoutFlow() throws Exception {
		String loginBody =
				"""
				{"tenantCode":"DEMO","login":"admin@demo.com","password":"Password1!"}
				""";

		MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.role").value("ADMIN"))
				.andReturn();

		String refreshToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.refreshToken");

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + refreshToken + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());

		mockMvc.perform(post("/api/v1/auth/logout")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + refreshToken + "\"}"))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/v1/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + refreshToken + "\"}"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void loginRejectsInvalidCredentials() throws Exception {
		String loginBody =
				"""
				{"tenantCode":"DEMO","login":"admin@demo.com","password":"wrong"}
				""";

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginBody))
				.andExpect(status().isUnauthorized());
	}
}
