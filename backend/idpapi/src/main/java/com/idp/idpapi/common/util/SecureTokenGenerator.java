package com.idp.idpapi.common.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class SecureTokenGenerator {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int DEFAULT_TOKEN_SIZE = 48;

    private SecureTokenGenerator() {
    }

    public static String generate() {
        byte[] bytes = new byte[DEFAULT_TOKEN_SIZE];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
