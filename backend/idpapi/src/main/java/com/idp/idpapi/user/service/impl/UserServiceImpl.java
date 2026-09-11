package com.idp.idpapi.user.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.common.exception.BadRequestException;
import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.department.entity.Department;
import com.idp.idpapi.department.repository.DepartmentRepository;
import com.idp.idpapi.role.entity.Role;
import com.idp.idpapi.role.repository.RoleRepository;
import com.idp.idpapi.user.dto.request.UserAvatarUpdateRequest;
import com.idp.idpapi.user.dto.request.UserCreateRequest;
import com.idp.idpapi.user.dto.request.UserRoleAssignRequest;
import com.idp.idpapi.user.dto.request.UserStatusUpdateRequest;
import com.idp.idpapi.user.dto.request.UserUpdateRequest;
import com.idp.idpapi.user.dto.response.UserResponse;
import com.idp.idpapi.user.entity.User;
import com.idp.idpapi.user.entity.UserRole;
import com.idp.idpapi.user.entity.UserRoleId;
import com.idp.idpapi.user.entity.UserStatus;
import com.idp.idpapi.user.mapper.UserMapper;
import com.idp.idpapi.user.repository.UserRepository;
import com.idp.idpapi.user.repository.UserRoleRepository;
import com.idp.idpapi.user.repository.UserSpecifications;
import com.idp.idpapi.user.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public UserServiceImpl(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RoleRepository roleRepository,
            DepartmentRepository departmentRepository,
            PasswordEncoder passwordEncoder,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAll(String keyword, Integer departmentId, UserStatus status, int page, int size) {
        Specification<User> specification = UserSpecifications.notDeleted()
                .and(UserSpecifications.keyword(keyword))
                .and(UserSpecifications.departmentId(departmentId))
                .and(UserSpecifications.status(status));
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(userRepository.findAll(specification, pageable)
                .map(user -> userMapper.toResponse(user, getRoleNames(user.getUserId()))));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(Integer userId) {
        User user = getUser(userId);
        return userMapper.toResponse(user, getRoleNames(userId));
    }

    @Override
    public UserResponse create(UserCreateRequest request, Integer currentUserId) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(email)) {
            throw new BadRequestException("Email da ton tai.");
        }

        Map<Integer, Role> roles = getRoles(request.roleIds());
        User user = new User();
        user.setDepartment(getDepartment(request.departmentId()));
        userMapper.applyCreateRequest(user, request);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPasswordChangedAt(LocalDateTime.now());
        user = userRepository.save(user);
        assignRolesInternal(user, roles, currentUserId);
        return userMapper.toResponse(user, getRoleNames(user.getUserId()));
    }

    @Override
    public UserResponse update(Integer userId, UserUpdateRequest request) {
        User user = getUser(userId);
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCaseAndUserIdNotAndIsDeletedFalse(email, userId)) {
            throw new BadRequestException("Email da ton tai.");
        }
        user.setDepartment(getDepartment(request.departmentId()));
        userMapper.applyUpdateRequest(user, request);
        user = userRepository.save(user);
        return userMapper.toResponse(user, getRoleNames(userId));
    }

    @Override
    public void delete(Integer userId) {
        User user = getUser(userId);
        user.setIsDeleted(Boolean.TRUE);
        userRepository.save(user);
    }

    @Override
    public UserResponse updateStatus(Integer userId, UserStatusUpdateRequest request) {
        User user = getUser(userId);
        user.setStatus(request.status());
        return userMapper.toResponse(userRepository.save(user), getRoleNames(userId));
    }

    @Override
    public UserResponse updateAvatar(Integer userId, UserAvatarUpdateRequest request) {
        User user = getUser(userId);
        user.setAvatar(request.avatar().trim());
        return userMapper.toResponse(userRepository.save(user), getRoleNames(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getRoleNames(Integer userId) {
        getUser(userId);
        return userRoleRepository.findRoleNamesByUserId(userId);
    }

    @Override
    public UserResponse assignRoles(Integer userId, UserRoleAssignRequest request, Integer currentUserId) {
        User user = getUser(userId);
        Map<Integer, Role> roles = getRoles(request.roleIds());
        assignRolesInternal(user, roles, currentUserId);
        return userMapper.toResponse(user, getRoleNames(userId));
    }

    @Override
    public void revokeRole(Integer userId, Integer roleId) {
        getUser(userId);
        roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay role."));
        userRoleRepository.deleteByUserUserIdAndRoleRoleId(userId, roleId);
    }

    private void assignRolesInternal(User user, Map<Integer, Role> roles, Integer currentUserId) {
        for (Map.Entry<Integer, Role> entry : roles.entrySet()) {
            UserRoleId id = new UserRoleId(user.getUserId(), entry.getKey());
            if (userRoleRepository.existsById(id)) {
                continue;
            }
            UserRole userRole = new UserRole();
            userRole.setId(id);
            userRole.setUser(user);
            userRole.setRole(entry.getValue());
            userRole.setAssignedBy(currentUserId);
            userRoleRepository.save(userRole);
        }
    }

    private User getUser(Integer userId) {
        return userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung."));
    }

    private Department getDepartment(Integer departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phong ban."));
    }

    private Map<Integer, Role> getRoles(Iterable<Integer> roleIds) {
        List<Integer> ids = new java.util.ArrayList<>();
        roleIds.forEach(ids::add);
        List<Role> roles = roleRepository.findAllById(ids);
        if (roles.size() != ids.size()) {
            throw new ResourceNotFoundException("Co role khong ton tai.");
        }
        return roles.stream().collect(Collectors.toMap(Role::getRoleId, Function.identity()));
    }
}
