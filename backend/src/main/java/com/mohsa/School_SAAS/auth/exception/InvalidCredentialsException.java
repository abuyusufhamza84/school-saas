package com.mohsa.School_SAAS.auth.exception;

public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("Invalid tenant, login, or password");
	}
}
