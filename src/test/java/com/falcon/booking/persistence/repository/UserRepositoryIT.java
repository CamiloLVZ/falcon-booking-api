package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.entity.UserRoleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRepositoryIT extends BaseRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private UserEntity createUser(String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setDisabled(false);
        return user;
    }

    @DisplayName("Should return user when email exists")
    @Test
    void shouldReturnUser_findByEmail() {
        UserEntity user = userRepository.save(createUser("client1@test.com"));

        Optional<UserEntity> userFound = userRepository.findByEmail("client1@test.com");

        assertThat(userFound).isPresent();
        assertThat(userFound.get().getId()).isEqualTo(user.getId());
        assertThat(userFound.get().getEmail()).isEqualTo("client1@test.com");
    }

    @DisplayName("Should return user with user roles relation when it exists")
    @Test
    void shouldReturnUserWithRoles_findByEmail() {
        RoleEntity role = roleRepository.save(new RoleEntity("ADMINISTRATOR"));
        UserEntity user = userRepository.save(createUser("admin1@test.com"));
        userRoleRepository.save(new UserRoleEntity(user, role));

        Optional<UserEntity> userFound = userRepository.findByEmail("admin1@test.com");

        assertThat(userFound).isPresent();
        assertThat(userFound.get().getUserRoles()).hasSize(1);
        UserRoleEntity relation = userFound.get().getUserRoles().iterator().next();
        assertThat(relation.getRole().getName()).isEqualTo("ADMINISTRATOR");
    }

    @DisplayName("Should return empty optional when email does not exist")
    @Test
    void shouldReturnEmpty_findByEmail() {
        Optional<UserEntity> userFound = userRepository.findByEmail("missing@test.com");

        assertThat(userFound).isEmpty();
    }
}
