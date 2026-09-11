package com.idp.idpapi.department.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.idp.idpapi.department.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {

    boolean existsByDepartmentNameIgnoreCase(String departmentName);

    boolean existsByDepartmentNameIgnoreCaseAndDepartmentIdNot(String departmentName, Integer departmentId);
}
