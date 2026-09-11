package com.idp.idpapi.ai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.ai.entity.AIClause;

public interface AIClauseRepository extends JpaRepository<AIClause, Integer> {

    List<AIClause> findByVersionVersionIdOrderByPageNumberAscClauseIdAsc(Integer versionId);

    void deleteByVersionVersionId(Integer versionId);
}
