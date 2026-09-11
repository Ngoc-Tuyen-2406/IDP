package com.idp.idpapi.auth.exception;

import com.idp.idpapi.common.exception.BadRequestException;

public class TokenValidationException extends BadRequestException {

    public TokenValidationException(String message) {
        super(message);
    }
}
