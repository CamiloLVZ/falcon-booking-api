package com.falcon.booking.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UserEntityTest {

    private RoleEntity createRole(Long id, String name) {
        RoleEntity role = new RoleEntity(name);
        ReflectionTestUtils.setField(role, "id", id);
        return role;
    }

    private UserEntity createUser(Long id, String email) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setEmail(email);
        user.setUserRoles(new HashSet<>());
        return user;
    }

    @DisplayName("Should return roles from user roles relation")
    @Test
    void shouldReturnRoles_getRoles() {
        UserEntity user = createUser(1L, "admin@test.com");
        RoleEntity adminRole = createRole(2L, "ADMIN");
        RoleEntity clientRole = createRole(3L, "CLIENT");
        user.setUserRoles(Set.of(new UserRoleEntity(user, adminRole), new UserRoleEntity(user, clientRole)));

        Set<RoleEntity> roles = user.getRoles();

        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(RoleEntity::getName).containsExactlyInAnyOrder("ADMIN", "CLIENT");
    }

    @DisplayName("Should return empty set when user roles is null")
    @Test
    void shouldReturnEmptyRoles_getRoles() {
        UserEntity user = createUser(1L, "client@test.com");
        user.setUserRoles(null);

        Set<RoleEntity> roles = user.getRoles();

        assertThat(roles).isEmpty();
    }
}
