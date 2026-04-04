package com.falcon.booking.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class RoleEntityTest {

    @DisplayName("Should create role with name through constructor")
    @Test
    void shouldCreateRole_constructor() {
        RoleEntity role = new RoleEntity("ADMIN");

        assertThat(role.getName()).isEqualTo("ADMIN");
    }

    @DisplayName("Should return same persisted id after setting id")
    @Test
    void shouldReturnId_getId() {
        RoleEntity role = new RoleEntity("CLIENT");
        ReflectionTestUtils.setField(role, "id", 15L);

        assertThat(role.getId()).isEqualTo(15L);
    }
}
