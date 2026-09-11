package com.idp.idpapi.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.auth.entity.EmailVerificationToken;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Integer> {

    Optional<EmailVerificationToken> findByToken(String token);

    long deleteByUserUserIdAndVerifiedAtIsNull(Integer userId);
}
