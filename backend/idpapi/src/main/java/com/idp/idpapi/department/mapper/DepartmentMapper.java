package com.idp.idpapi.department.mapper;

import org.springframework.stereotype.Component;

import com.idp.idpapi.department.dto.request.DepartmentUpsertRequest;
import com.idp.idpapi.department.dto.response.DepartmentResponse;
import com.idp.idpapi.department.entity.Department;

@Component
public class DepartmentMapper {

    public void updateEntity(Department entity, DepartmentUpsertRequest request) {
        entity.setDepartmentName(request.departmentName().trim());
        entity.setDescription(request.description());
    }

    public DepartmentResponse toResponse(Department entity) {
        return new DepartmentResponse(
                entity.getDepartmentId(),
                entity.getDepartmentName(),
                entity.getDescription());
    }
}
