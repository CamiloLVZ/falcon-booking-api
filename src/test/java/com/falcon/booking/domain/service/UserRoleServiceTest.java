package com.falcon.booking.domain.service;

import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.entity.UserRoleEntity;
import com.falcon.booking.persistence.repository.UserRoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserRoleServiceTest {

    @Mock
    private UserRoleRepository userRoleRepository;

    @InjectMocks
    private UserRoleService userRoleService;

    private UserEntity createUser(Long id, String email) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setEmail(email);
        user.setUserRoles(new HashSet<>());
        return user;
    }

    private RoleEntity createRole(Long id, String roleName) {
        RoleEntity role = new RoleEntity(roleName);
        ReflectionTestUtils.setField(role, "id", id);
        return role;
    }

    @DisplayName("Should return existing user role when relation already exists")
    @Test
    void shouldReturnExistingUserRole_addUserRole() {
        UserEntity user = createUser(1L, "client@test.com");
        RoleEntity role = createRole(2L, "CLIENT");
        UserRoleEntity existingUserRole = new UserRoleEntity(user, role);
        given(userRoleRepository.findByUserAndRole(user, role)).willReturn(Optional.of(existingUserRole));

        UserRoleEntity result = userRoleService.addUserRole(user, role);

        assertThat(result).isEqualTo(existingUserRole);
        verify(userRoleRepository).findByUserAndRole(user, role);
    }

    @DisplayName("Should create and save user role when relation does not exist")
    @Test
    void shouldCreateAndSaveUserRole_addUserRole() {
        UserEntity user = createUser(1L, "admin@test.com");
        RoleEntity role = createRole(3L, "ADMIN");
        UserRoleEntity expected = new UserRoleEntity(user, role);

        given(userRoleRepository.findByUserAndRole(user, role)).willReturn(Optional.empty());
        given(userRoleRepository.save(org.mockito.ArgumentMatchers.any(UserRoleEntity.class))).willReturn(expected);

        UserRoleEntity result = userRoleService.addUserRole(user, role);

        assertThat(result).isEqualTo(expected);
        assertThat(user.getUserRoles()).hasSize(1);
        verify(userRoleRepository).findByUserAndRole(user, role);
        verify(userRoleRepository).save(org.mockito.ArgumentMatchers.any(UserRoleEntity.class));
    }
}
