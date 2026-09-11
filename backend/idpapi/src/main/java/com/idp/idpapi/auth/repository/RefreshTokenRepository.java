package com.idp.idpapi.auth.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.idp.idpapi.auth.entity.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByTokenIdAndUserUserId(Integer tokenId, Integer userId);

    @Query("""
            select rt
            from RefreshToken rt
            where rt.user.userId = :userId
              and rt.revoked = false
              and (rt.expiresAt is null or rt.expiresAt > :currentTime)
            order by rt.createdAt desc
            """)
    Page<RefreshToken> findActiveSessions(
            @Param("userId") Integer userId,
            @Param("currentTime") LocalDateTime currentTime,
            Pageable pageable);

    @Modifying
    @Query("""
            update RefreshToken rt
            set rt.revoked = true
            where rt.user.userId = :userId
              and rt.revoked = false
            """)
    int revokeAllByUserId(@Param("userId") Integer userId);
}
