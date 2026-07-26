package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.RoleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class RoleRepositoryIT extends BaseRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @DisplayName("Should return role when name exists")
    @Test
    void shouldReturnRole_findByName() {
        RoleEntity role = roleRepository.save(new RoleEntity("MANAGER"));

        Optional<RoleEntity> roleFound = roleRepository.findByName("MANAGER");

        assertThat(roleFound).isPresent();
        assertThat(roleFound.get().getId()).isEqualTo(role.getId());
        assertThat(roleFound.get().getName()).isEqualTo("MANAGER");
    }

    @DisplayName("Should return empty optional when name does not exist")
    @Test
    void shouldReturnEmpty_findByName() {
        Optional<RoleEntity> roleFound = roleRepository.findByName("ROLE_NOT_EXIST");

        assertThat(roleFound).isEmpty();
    }
}
