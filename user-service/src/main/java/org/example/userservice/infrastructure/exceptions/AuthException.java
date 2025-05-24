package org.example.userservice.infrastructure.exceptions;

import org.example.common.exceptions.status_code_exceptions.UnauthorizedException;

public class AuthException extends UnauthorizedException {
    public AuthException(String message) {
        super(message);
    }
}