package com.idp.idpapi.permission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.permission.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Integer> {

    boolean existsByPermissionNameIgnoreCase(String permissionName);

    boolean existsByPermissionNameIgnoreCaseAndPermissionIdNot(String permissionName, Integer permissionId);
}
