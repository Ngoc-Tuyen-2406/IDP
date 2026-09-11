package com.idp.idpapi.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.auth.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    long deleteByUserUserIdAndUsedFalse(Integer userId);
}
