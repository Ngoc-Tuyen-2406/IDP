package com.idp.idpapi.auth.exception;

import com.idp.idpapi.common.exception.UnauthorizedException;

public class AuthenticationFailedException extends UnauthorizedException {

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
