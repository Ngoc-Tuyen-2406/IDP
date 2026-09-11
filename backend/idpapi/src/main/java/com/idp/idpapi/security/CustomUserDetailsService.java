package com.idp.idpapi.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.idp.idpapi.common.exception.ResourceNotFoundException;
import com.idp.idpapi.role.repository.RolePermissionRepository;
import com.idp.idpapi.user.entity.User;
import com.idp.idpapi.user.repository.UserRepository;
import com.idp.idpapi.user.repository.UserRoleRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public CustomUserDetailsService(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            RolePermissionRepository rolePermissionRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmailIgnoreCaseAndIsDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email đã cung cấp."));
        return buildUserDetails(user);
    }

    public SecurityUserDetails loadUserById(Integer userId) {
        User user = userRepository.findByUserIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hiện tại."));
        return buildUserDetails(user);
    }

    private SecurityUserDetails buildUserDetails(User user) {
        List<Integer> roleIds = userRoleRepository.findRoleIdsByUserId(user.getUserId());
        List<String> roleNames = userRoleRepository.findRoleNamesByUserId(user.getUserId());
        Set<String> permissions = roleIds.isEmpty()
                ? Set.of()
                : rolePermissionRepository.findPermissionNamesByRoleIds(roleIds);

        return new SecurityUserDetails(
                user.getUserId(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getFullName(),
                user.getStatus(),
                Boolean.TRUE.equals(user.getEmailVerified()),
                Boolean.TRUE.equals(user.getIsDeleted()),
                buildAuthorities(roleNames, permissions));
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(List<String> roles, Set<String> permissions) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .forEach(authorities::add);
        permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        return authorities;
    }
}
