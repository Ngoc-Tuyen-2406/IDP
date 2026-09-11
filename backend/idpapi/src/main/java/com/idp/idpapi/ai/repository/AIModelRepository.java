package com.idp.idpapi.ai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.ai.entity.AIModel;

public interface AIModelRepository extends JpaRepository<AIModel, Integer> {

    Optional<AIModel> findByModelNameIgnoreCase(String modelName);
}
