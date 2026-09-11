package com.idp.idpapi.permission.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idp.idpapi.common.exception.BadRequestException;
import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.permission.dto.request.PermissionUpsertRequest;
import com.idp.idpapi.permission.dto.response.PermissionResponse;
import com.idp.idpapi.permission.entity.Permission;
import com.idp.idpapi.permission.mapper.PermissionMapper;
import com.idp.idpapi.permission.repository.PermissionRepository;
import com.idp.idpapi.permission.service.PermissionService;

@Service
@Transactional
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    public PermissionServiceImpl(
            PermissionRepository permissionRepository,
            PermissionMapper permissionMapper) {
        this.permissionRepository = permissionRepository;
        this.permissionMapper = permissionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAll() {
        return permissionRepository.findAll()
                .stream()
                .map(permissionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getById(Integer permissionId) {
        return permissionMapper.toResponse(getEntity(permissionId));
    }

    @Override
    public PermissionResponse create(PermissionUpsertRequest request) {
        if (permissionRepository.existsByPermissionNameIgnoreCase(request.permissionName().trim())) {
            throw new BadRequestException("Permission da ton tai.");
        }
        Permission entity = new Permission();
        permissionMapper.updateEntity(entity, request);
        return permissionMapper.toResponse(permissionRepository.save(entity));
    }

    @Override
    public PermissionResponse update(Integer permissionId, PermissionUpsertRequest request) {
        Permission entity = getEntity(permissionId);
        if (permissionRepository.existsByPermissionNameIgnoreCaseAndPermissionIdNot(
                request.permissionName().trim(),
                permissionId)) {
            throw new BadRequestException("Permission da ton tai.");
        }
        permissionMapper.updateEntity(entity, request);
        return permissionMapper.toResponse(permissionRepository.save(entity));
    }

    @Override
    public void delete(Integer permissionId) {
        permissionRepository.delete(getEntity(permissionId));
    }

    private Permission getEntity(Integer permissionId) {
        return permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay permission."));
    }
}
