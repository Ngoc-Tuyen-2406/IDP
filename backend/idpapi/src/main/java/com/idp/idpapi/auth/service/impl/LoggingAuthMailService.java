package com.idp.idpapi.auth.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.idp.idpapi.auth.service.AuthMailService;
import com.idp.idpapi.user.entity.User;

@Service
public class LoggingAuthMailService implements AuthMailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAuthMailService.class);

    @Override
    public void sendPasswordReset(User user, String rawToken) {
        LOGGER.info("Password reset token generated for user={} token={}", user.getEmail(), mask(rawToken));
    }

    @Override
    public void sendVerificationEmail(User user, String rawToken) {
        LOGGER.info("Email verification token generated for user={} token={}", user.getEmail(), mask(rawToken));
    }

    private String mask(String token) {
        if (token == null || token.length() < 10) {
            return "masked";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
