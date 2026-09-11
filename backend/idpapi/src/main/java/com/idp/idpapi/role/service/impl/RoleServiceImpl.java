package com.idp.idpapi.role.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idp.idpapi.common.exception.BadRequestException;
import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.permission.dto.response.PermissionResponse;
import com.idp.idpapi.permission.entity.Permission;
import com.idp.idpapi.permission.mapper.PermissionMapper;
import com.idp.idpapi.permission.repository.PermissionRepository;
import com.idp.idpapi.role.dto.request.RolePermissionAssignRequest;
import com.idp.idpapi.role.dto.request.RoleUpsertRequest;
import com.idp.idpapi.role.dto.response.RoleDetailResponse;
import com.idp.idpapi.role.dto.response.RoleResponse;
import com.idp.idpapi.role.entity.Role;
import com.idp.idpapi.role.entity.RolePermission;
import com.idp.idpapi.role.entity.RolePermissionId;
import com.idp.idpapi.role.mapper.RoleMapper;
import com.idp.idpapi.role.repository.RolePermissionRepository;
import com.idp.idpapi.role.repository.RoleRepository;
import com.idp.idpapi.role.service.RoleService;
import com.idp.idpapi.user.repository.UserRoleRepository;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;

    public RoleServiceImpl(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RolePermissionRepository rolePermissionRepository,
            UserRoleRepository userRoleRepository,
            RoleMapper roleMapper,
            PermissionMapper permissionMapper) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAll() {
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoleDetailResponse getById(Integer roleId) {
        Role role = getRole(roleId);
        return roleMapper.toDetailResponse(role, getPermissions(roleId));
    }

    @Override
    public RoleResponse create(RoleUpsertRequest request) {
        String roleName = request.roleName().trim().toUpperCase();
        if (roleRepository.existsByRoleNameIgnoreCase(roleName)) {
            throw new BadRequestException("Role da ton tai.");
        }
        Role entity = new Role();
        roleMapper.updateEntity(entity, request);
        return roleMapper.toResponse(roleRepository.save(entity));
    }

    @Override
    public RoleResponse update(Integer roleId, RoleUpsertRequest request) {
        Role entity = getRole(roleId);
        String roleName = request.roleName().trim().toUpperCase();
        if (roleRepository.existsByRoleNameIgnoreCaseAndRoleIdNot(roleName, roleId)) {
            throw new BadRequestException("Role da ton tai.");
        }
        roleMapper.updateEntity(entity, request);
        return roleMapper.toResponse(roleRepository.save(entity));
    }

    @Override
    public void delete(Integer roleId) {
        if (!userRoleRepository.findUserIdsByRoleId(roleId).isEmpty()) {
            throw new BadRequestException("Khong the xoa role dang duoc gan cho nguoi dung.");
        }
        roleRepository.delete(getRole(roleId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getPermissions(Integer roleId) {
        getRole(roleId);
        Set<Integer> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(roleId);
        if (permissionIds.isEmpty()) {
            return List.of();
        }
        return permissionRepository.findAllById(permissionIds)
                .stream()
                .map(permissionMapper::toResponse)
                .toList();
    }

    @Override
    public RoleDetailResponse assignPermissions(Integer roleId, RolePermissionAssignRequest request) {
        Role role = getRole(roleId);
        Map<Integer, Permission> permissions = permissionRepository.findAllById(request.permissionIds())
                .stream()
                .collect(Collectors.toMap(Permission::getPermissionId, Function.identity()));
        if (permissions.size() != request.permissionIds().size()) {
            throw new ResourceNotFoundException("Co permission khong ton tai.");
        }

        for (Integer permissionId : request.permissionIds()) {
            RolePermissionId id = new RolePermissionId(roleId, permissionId);
            if (rolePermissionRepository.existsById(id)) {
                continue;
            }
            RolePermission rolePermission = new RolePermission();
            rolePermission.setId(id);
            rolePermission.setRole(role);
            rolePermission.setPermission(permissions.get(permissionId));
            rolePermissionRepository.save(rolePermission);
        }

        return roleMapper.toDetailResponse(role, getPermissions(roleId));
    }

    @Override
    public void revokePermission(Integer roleId, Integer permissionId) {
        getRole(roleId);
        permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay permission."));
        rolePermissionRepository.deleteById(new RolePermissionId(roleId, permissionId));
    }

    private Role getRole(Integer roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay role."));
    }
}
