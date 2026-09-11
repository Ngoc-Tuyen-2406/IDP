package com.idp.idpapi.department.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idp.idpapi.common.exception.BadRequestException;
import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.department.dto.request.DepartmentUpsertRequest;
import com.idp.idpapi.department.dto.response.DepartmentResponse;
import com.idp.idpapi.department.entity.Department;
import com.idp.idpapi.department.mapper.DepartmentMapper;
import com.idp.idpapi.department.repository.DepartmentRepository;
import com.idp.idpapi.department.service.DepartmentService;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(
            DepartmentRepository departmentRepository,
            DepartmentMapper departmentMapper) {
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll()
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getById(Integer departmentId) {
        return departmentMapper.toResponse(getEntity(departmentId));
    }

    @Override
    public DepartmentResponse create(DepartmentUpsertRequest request) {
        if (departmentRepository.existsByDepartmentNameIgnoreCase(request.departmentName().trim())) {
            throw new BadRequestException("Phong ban da ton tai.");
        }
        Department entity = new Department();
        departmentMapper.updateEntity(entity, request);
        return departmentMapper.toResponse(departmentRepository.save(entity));
    }

    @Override
    public DepartmentResponse update(Integer departmentId, DepartmentUpsertRequest request) {
        Department entity = getEntity(departmentId);
        if (departmentRepository.existsByDepartmentNameIgnoreCaseAndDepartmentIdNot(
                request.departmentName().trim(),
                departmentId)) {
            throw new BadRequestException("Phong ban da ton tai.");
        }
        departmentMapper.updateEntity(entity, request);
        return departmentMapper.toResponse(departmentRepository.save(entity));
    }

    @Override
    public void delete(Integer departmentId) {
        departmentRepository.delete(getEntity(departmentId));
    }

    private Department getEntity(Integer departmentId) {
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phong ban."));
    }
}
