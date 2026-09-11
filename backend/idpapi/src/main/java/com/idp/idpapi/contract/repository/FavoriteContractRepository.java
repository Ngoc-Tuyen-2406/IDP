package com.idp.idpapi.contract.repository;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.idp.idpapi.contract.entity.FavoriteContract;
import com.idp.idpapi.contract.entity.FavoriteContractId;

public interface FavoriteContractRepository extends JpaRepository<FavoriteContract, FavoriteContractId> {

    boolean existsByUserUserIdAndContractContractId(Integer userId, Integer contractId);

    Page<FavoriteContract> findByUserUserId(Integer userId, Pageable pageable);

    @Query("""
            select fc.contract.contractId
            from FavoriteContract fc
            where fc.user.userId = :userId
            """)
    Set<Integer> findFavoriteContractIdsByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query("""
            delete from FavoriteContract fc
            where fc.user.userId = :userId
              and fc.contract.contractId = :contractId
            """)
    int deleteByUserIdAndContractId(@Param("userId") Integer userId, @Param("contractId") Integer contractId);
}
