package com.falcon.booking.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRoleEntityTest {

    private UserEntity createUser(Long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        return user;
    }

    private RoleEntity createRole(Long id, String name) {
        RoleEntity role = new RoleEntity(name);
        ReflectionTestUtils.setField(role, "id", id);
        return role;
    }

    @DisplayName("Equals methods for entities with same user and role should return true")
    @Test
    void shouldReturnTrue_sameUserRoleInEquals() {
        UserRoleEntity userRole1 = new UserRoleEntity(createUser(1L), createRole(2L, "ADMIN"));
        UserRoleEntity userRole2 = new UserRoleEntity(createUser(1L), createRole(2L, "ADMIN"));

        boolean result = userRole1.equals(userRole2);

        assertThat(result).isTrue();
    }

    @DisplayName("Equals methods for entities with different user and role should return false")
    @Test
    void shouldReturnFalse_differentUserRoleInEquals() {
        UserRoleEntity userRole1 = new UserRoleEntity(createUser(1L), createRole(2L, "ADMIN"));
        UserRoleEntity userRole2 = new UserRoleEntity(createUser(2L), createRole(3L, "CLIENT"));

        boolean result = userRole1.equals(userRole2);

        assertThat(result).isFalse();
    }

    @DisplayName("Hash code for entities with same user and role should be equal")
    @Test
    void shouldBeEqual_sameUserRoleHashCode() {
        UserRoleEntity userRole1 = new UserRoleEntity(createUser(1L), createRole(2L, "ADMIN"));
        UserRoleEntity userRole2 = new UserRoleEntity(createUser(1L), createRole(2L, "ADMIN"));

        int hashCode1 = userRole1.hashCode();
        int hashCode2 = userRole2.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @DisplayName("Hash code for entities with different user and role should not be equal")
    @Test
    void shouldNotBeEqual_differentUserRoleHashCode() {
        UserRoleEntity userRole1 = new UserRoleEntity(createUser(1L), createRole(2L, "ADMIN"));
        UserRoleEntity userRole2 = new UserRoleEntity(createUser(3L), createRole(4L, "CLIENT"));

        int hashCode1 = userRole1.hashCode();
        int hashCode2 = userRole2.hashCode();

        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }
}
