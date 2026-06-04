package com.mohsa.School_SAAS.auth.security;

import com.mohsa.School_SAAS.auth.entity.User;
import com.mohsa.School_SAAS.auth.repository.UserRepository;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public AuthUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) {
		throw new UnsupportedOperationException("Use tenant-scoped login instead");
	}

	@Transactional(readOnly = true)
	public AuthenticatedUser loadById(UUID userId) {
		User user = userRepository
				.findByIdAndActiveTrue(userId)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
		return toAuthenticatedUser(user);
	}

	public static AuthenticatedUser toAuthenticatedUser(User user) {
		return new AuthenticatedUser(
				user.getId(),
				user.getTenant().getId(),
				user.getTenant().getTenantCode(),
				user.getUsername(),
				user.getPasswordHash(),
				user.getRole().getRoleName(),
				user.isActive());
	}
}
