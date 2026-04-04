package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.entity.UserRoleEntity;
import com.falcon.booking.persistence.entity.UserRoleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("tests")
public class UserRoleRepositoryTest {

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

    @DisplayName("Should return true when user has role")
    @Test
    void shouldReturnTrue_existsByEmailAndRoleName() {
        RoleEntity role = roleRepository.save(new RoleEntity("MANAGER"));
        UserEntity user = userRepository.save(createUser("manager@test.com"));
        userRoleRepository.save(new UserRoleEntity(user, role));

        boolean exists = userRoleRepository.ExistsByEmailAndRoleName("manager@test.com", "MANAGER");

        assertThat(exists).isTrue();
    }

    @DisplayName("Should return false when user does not have role")
    @Test
    void shouldReturnFalse_existsByEmailAndRoleName() {
        RoleEntity role = roleRepository.save(new RoleEntity("MANAGER"));
        UserEntity user = userRepository.save(createUser("manager@test.com"));
        userRoleRepository.save(new UserRoleEntity(user, role));

        boolean exists = userRoleRepository.ExistsByEmailAndRoleName("manager@test.com", "ADMIN");

        assertThat(exists).isFalse();
    }

    @DisplayName("Should return user role relation by user and role")
    @Test
    void shouldReturnUserRole_findByUserAndRole() {
        RoleEntity role = roleRepository.save(new RoleEntity("SUPPORT"));
        UserEntity user = userRepository.save(createUser("support@test.com"));
        UserRoleEntity saved = userRoleRepository.save(new UserRoleEntity(user, role));

        Optional<UserRoleEntity> relationFound = userRoleRepository.findByUserAndRole(user, role);

        assertThat(relationFound).isPresent();
        assertThat(relationFound.get()).isEqualTo(saved);
    }

    @DisplayName("Should return user role relation by composite id")
    @Test
    void shouldReturnUserRole_findById() {
        RoleEntity role = roleRepository.save(new RoleEntity("AGENT"));
        UserEntity user = userRepository.save(createUser("agent@test.com"));
        UserRoleEntity saved = userRoleRepository.save(new UserRoleEntity(user, role));

        Optional<UserRoleEntity> relationFound = userRoleRepository.findById(new UserRoleId(user.getId(), role.getId()));

        assertThat(relationFound).isPresent();
        assertThat(relationFound.get()).isEqualTo(saved);
    }
}
