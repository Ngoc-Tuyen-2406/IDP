package com.idp.idpapi.user.repository;

import org.springframework.data.jpa.domain.Specification;

import com.idp.idpapi.user.entity.User;
import com.idp.idpapi.user.entity.UserStatus;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<User> notDeleted() {
        return (root, query, builder) -> builder.isFalse(root.get("isDeleted"));
    }

    public static Specification<User> keyword(String keyword) {
        return (root, query, builder) -> {
            if (keyword == null || keyword.isBlank()) {
                return builder.conjunction();
            }
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("fullName")), pattern),
                    builder.like(builder.lower(root.get("email")), pattern));
        };
    }

    public static Specification<User> departmentId(Integer departmentId) {
        return (root, query, builder) -> {
            if (departmentId == null) {
                return builder.conjunction();
            }
            return builder.equal(root.join("department").get("departmentId"), departmentId);
        };
    }

    public static Specification<User> status(UserStatus status) {
        return (root, query, builder) -> status == null
                ? builder.conjunction()
                : builder.equal(root.get("status"), status);
    }
}
