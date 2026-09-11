package com.idp.idpapi.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.user.entity.User;

public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailIgnoreCaseAndIsDeletedFalse(String email);

    Optional<User> findByUserIdAndIsDeletedFalse(Integer userId);

    boolean existsByEmailIgnoreCaseAndIsDeletedFalse(String email);

    boolean existsByEmailIgnoreCaseAndUserIdNotAndIsDeletedFalse(String email, Integer userId);
}
