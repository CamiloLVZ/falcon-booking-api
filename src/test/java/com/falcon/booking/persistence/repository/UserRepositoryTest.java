package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.entity.UserRoleEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("tests")
public class UserRepositoryTest {

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
        UserEntity user = userRepository.save(createUser("client@test.com"));

        Optional<UserEntity> userFound = userRepository.findByEmail("client@test.com");

        assertThat(userFound).isPresent();
        assertThat(userFound.get().getId()).isEqualTo(user.getId());
        assertThat(userFound.get().getEmail()).isEqualTo("client@test.com");
    }

    @DisplayName("Should return user with user roles relation when it exists")
    @Test
    void shouldReturnUserWithRoles_findByEmail() {
        RoleEntity role = roleRepository.save(new RoleEntity("ADMIN"));
        UserEntity user = userRepository.save(createUser("admin@test.com"));
        userRoleRepository.save(new UserRoleEntity(user, role));

        Optional<UserEntity> userFound = userRepository.findByEmail("admin@test.com");

        assertThat(userFound).isPresent();
        assertThat(userFound.get().getUserRoles()).hasSize(1);
        UserRoleEntity relation = userFound.get().getUserRoles().iterator().next();
        assertThat(relation.getRole().getName()).isEqualTo("ADMIN");
    }

    @DisplayName("Should return empty optional when email does not exist")
    @Test
    void shouldReturnEmpty_findByEmail() {
        Optional<UserEntity> userFound = userRepository.findByEmail("missing@test.com");

        assertThat(userFound).isEmpty();
    }
}
