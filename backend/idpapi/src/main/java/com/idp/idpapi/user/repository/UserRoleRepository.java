package com.idp.idpapi.user.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.idp.idpapi.user.entity.UserRole;
import com.idp.idpapi.user.entity.UserRoleId;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    @Query("""
            select ur.role.roleId
            from UserRole ur
            where ur.user.userId = :userId
            """)
    List<Integer> findRoleIdsByUserId(@Param("userId") Integer userId);

    @Query("""
            select ur.role.roleName
            from UserRole ur
            where ur.user.userId = :userId
            """)
    List<String> findRoleNamesByUserId(@Param("userId") Integer userId);

    List<UserRole> findByUserUserId(Integer userId);

    void deleteByUserUserIdAndRoleRoleId(Integer userId, Integer roleId);

    @Query("""
            select ur.user.userId
            from UserRole ur
            where ur.role.roleId = :roleId
            """)
    List<Integer> findUserIdsByRoleId(@Param("roleId") Integer roleId);

    @Query("""
            select ur.role.roleId
            from UserRole ur
            where ur.user.userId in :userIds
            """)
    List<Integer> findRoleIdsByUserIds(@Param("userIds") Collection<Integer> userIds);
}
