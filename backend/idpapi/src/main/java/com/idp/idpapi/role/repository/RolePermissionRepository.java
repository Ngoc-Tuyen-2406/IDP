package com.idp.idpapi.role.repository;

import java.util.Collection;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.idp.idpapi.role.entity.RolePermission;
import com.idp.idpapi.role.entity.RolePermissionId;

public interface RolePermissionRepository extends JpaRepository<RolePermission, RolePermissionId> {

    @Query("""
            select distinct rp.permission.permissionName
            from RolePermission rp
            where rp.role.roleId in :roleIds
            """)
    Set<String> findPermissionNamesByRoleIds(@Param("roleIds") Collection<Integer> roleIds);

    @Query("""
            select rp.permission.permissionId
            from RolePermission rp
            where rp.role.roleId = :roleId
            """)
    Set<Integer> findPermissionIdsByRoleId(@Param("roleId") Integer roleId);
}
