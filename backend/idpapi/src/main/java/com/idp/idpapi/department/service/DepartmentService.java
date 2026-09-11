package com.idp.idpapi.department.service;

import java.util.List;

import com.idp.idpapi.department.dto.request.DepartmentUpsertRequest;
import com.idp.idpapi.department.dto.response.DepartmentResponse;

public interface DepartmentService {

    List<DepartmentResponse> getAll();

    DepartmentResponse getById(Integer departmentId);

    DepartmentResponse create(DepartmentUpsertRequest request);

    DepartmentResponse update(Integer departmentId, DepartmentUpsertRequest request);

    void delete(Integer departmentId);
}
