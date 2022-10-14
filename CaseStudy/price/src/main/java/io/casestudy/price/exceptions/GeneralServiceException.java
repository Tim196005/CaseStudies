package io.casestudy.price.exceptions;

public class GeneralServiceException extends RuntimeException {

	private static final long serialVersionUID = -2351112163804714032L;

	public GeneralServiceException(String message, Throwable cause) {
		super(message, cause);
	}
}
