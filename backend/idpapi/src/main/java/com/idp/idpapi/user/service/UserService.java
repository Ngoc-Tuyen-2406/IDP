package com.idp.idpapi.user.service;

import java.util.List;

import com.idp.idpapi.common.api.PageResponse;
import com.idp.idpapi.user.dto.request.UserAvatarUpdateRequest;
import com.idp.idpapi.user.dto.request.UserCreateRequest;
import com.idp.idpapi.user.dto.request.UserRoleAssignRequest;
import com.idp.idpapi.user.dto.request.UserStatusUpdateRequest;
import com.idp.idpapi.user.dto.request.UserUpdateRequest;
import com.idp.idpapi.user.dto.response.UserResponse;
import com.idp.idpapi.user.entity.UserStatus;

public interface UserService {

    PageResponse<UserResponse> getAll(String keyword, Integer departmentId, UserStatus status, int page, int size);

    UserResponse getById(Integer userId);

    UserResponse create(UserCreateRequest request, Integer currentUserId);

    UserResponse update(Integer userId, UserUpdateRequest request);

    void delete(Integer userId);

    UserResponse updateStatus(Integer userId, UserStatusUpdateRequest request);

    UserResponse updateAvatar(Integer userId, UserAvatarUpdateRequest request);

    List<String> getRoleNames(Integer userId);

    UserResponse assignRoles(Integer userId, UserRoleAssignRequest request, Integer currentUserId);

    void revokeRole(Integer userId, Integer roleId);
}
