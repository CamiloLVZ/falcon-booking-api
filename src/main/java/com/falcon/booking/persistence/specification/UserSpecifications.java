package com.falcon.booking.persistence.specification;

import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.entity.UserRoleEntity;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    private UserSpecifications() {}

    public static Specification<UserEntity> hasEmailLike(String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
        };
    }

    public static Specification<UserEntity> hasDisabled(Boolean disabled) {
        return (root, query, cb) -> {
            if (disabled == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("disabled"), disabled);
        };
    }

    public static Specification<UserEntity> hasRoleName(String roleName) {
        return (root, query, cb) -> {
            if (roleName == null || roleName.isBlank()) {
                return cb.conjunction();
            }
            Join<UserEntity, UserRoleEntity> userRoles = root.join("userRoles");
            Join<UserRoleEntity, RoleEntity> role = userRoles.join("role");
            return cb.equal(role.get("name"), roleName);
        };
    }
}
