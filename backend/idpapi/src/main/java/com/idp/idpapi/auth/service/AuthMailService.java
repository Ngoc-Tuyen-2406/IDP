package com.idp.idpapi.auth.service;

import com.idp.idpapi.user.entity.User;

public interface AuthMailService {

    void sendPasswordReset(User user, String rawToken);

    void sendVerificationEmail(User user, String rawToken);
}
