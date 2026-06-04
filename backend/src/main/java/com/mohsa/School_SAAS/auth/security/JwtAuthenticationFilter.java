package com.mohsa.School_SAAS.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final AuthUserDetailsService authUserDetailsService;

	public JwtAuthenticationFilter(JwtService jwtService, AuthUserDetailsService authUserDetailsService) {
		this.jwtService = jwtService;
		this.authUserDetailsService = authUserDetailsService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			try {
				if (jwtService.isAccessToken(token)) {
					jwtService.validateToken(token);
					AuthenticatedUser user = authUserDetailsService.loadById(jwtService.extractUserId(token));
					SecurityContextHolder.getContext()
							.setAuthentication(new JwtAuthenticationToken(user, token));
				}
			}
			catch (InvalidTokenException | org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
				SecurityContextHolder.clearContext();
			}
		}
		filterChain.doFilter(request, response);
	}
}
