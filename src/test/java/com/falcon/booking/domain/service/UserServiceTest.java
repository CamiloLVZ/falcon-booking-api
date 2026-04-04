package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.User.UserAlreadyExistException;
import com.falcon.booking.domain.exception.User.UserNotFoundException;
import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.repository.UserRepository;
import com.falcon.booking.web.dto.user.CreateUserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleService roleService;

    @Mock
    private UserRoleService userRoleService;

    @InjectMocks
    private UserService userService;

    private UserEntity createUser(Long id, String email) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setDisabled(false);
        return user;
    }

    @DisplayName("Should return user when user exists")
    @Test
    void shouldReturnUser_getUserByEmail() {
        UserEntity user = createUser(1L, "client@test.com");
        given(userRepository.findByEmail("client@test.com")).willReturn(Optional.of(user));

        UserEntity result = userService.getUserByEmail("client@test.com");

        assertThat(result).isEqualTo(user);
        verify(userRepository).findByEmail("client@test.com");
    }

    @DisplayName("Should throw exception when user does not exist")
    @Test
    void shouldThrowException_getUserByEmail() {
        given(userRepository.findByEmail("ghost@test.com")).willReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.getUserByEmail("ghost@test.com"));

        assertThat(ex.getMessage()).contains("ghost@test.com");
        verify(userRepository).findByEmail("ghost@test.com");
    }

    @DisplayName("Should return built user when email is available")
    @Test
    void shouldReturnBuiltUser_buildUser() {
        CreateUserDto dto = new CreateUserDto("new@test.com", "password123");
        given(userRepository.findByEmail("new@test.com")).willReturn(Optional.empty());
        given(passwordEncoder.encode("password123")).willReturn("encoded-password");

        UserEntity result = userService.buildUser(dto);

        assertThat(result.getEmail()).isEqualTo("new@test.com");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.getDisabled()).isFalse();
        verify(userRepository).findByEmail("new@test.com");
        verify(passwordEncoder).encode("password123");
    }

    @DisplayName("Should throw exception when build user with existing email")
    @Test
    void shouldThrowException_buildUser() {
        CreateUserDto dto = new CreateUserDto("existing@test.com", "password123");
        given(userRepository.findByEmail("existing@test.com")).willReturn(Optional.of(createUser(1L, "existing@test.com")));

        UserAlreadyExistException ex = assertThrows(UserAlreadyExistException.class,
                () -> userService.buildUser(dto));

        assertThat(ex.getMessage()).contains("existing@test.com");
        verify(userRepository).findByEmail("existing@test.com");
    }

    @DisplayName("Should create client user with client role")
    @Test
    void shouldCreateClientUser_createClientUser() {
        CreateUserDto dto = new CreateUserDto("client@test.com", "password123");
        UserEntity built = createUser(null, "client@test.com");
        built.setPassword("encoded-password");
        UserEntity saved = createUser(10L, "client@test.com");
        RoleEntity clientRole = new RoleEntity("CLIENT");

        given(userRepository.findByEmail("client@test.com")).willReturn(Optional.empty(), Optional.empty());
        given(passwordEncoder.encode("password123")).willReturn("encoded-password");
        given(userRepository.save(any(UserEntity.class))).willReturn(saved);
        given(roleService.getRoleByName("CLIENT")).willReturn(clientRole);

        UserEntity result = userService.createClientUser(dto);

        assertThat(result).isEqualTo(saved);
        verify(userRepository).save(any(UserEntity.class));
        verify(roleService).getRoleByName("CLIENT");
        verify(userRoleService).addUserRole(saved, clientRole);
    }

    @DisplayName("Should create admin user with admin role")
    @Test
    void shouldCreateAdminUser_createAdminUser() {
        CreateUserDto dto = new CreateUserDto("admin@test.com", "password123");
        UserEntity saved = createUser(11L, "admin@test.com");
        RoleEntity adminRole = new RoleEntity("ADMIN");

        given(userRepository.findByEmail("admin@test.com")).willReturn(Optional.empty(), Optional.empty());
        given(passwordEncoder.encode("password123")).willReturn("encoded-password");
        given(userRepository.save(any(UserEntity.class))).willReturn(saved);
        given(roleService.getRoleByName("ADMIN")).willReturn(adminRole);

        UserEntity result = userService.createAdminUser(dto);

        assertThat(result).isEqualTo(saved);
        verify(userRepository).save(any(UserEntity.class));
        verify(roleService).getRoleByName("ADMIN");
        verify(userRoleService).addUserRole(saved, adminRole);
    }

    @DisplayName("Should return existing admin when admin already exists")
    @Test
    void shouldReturnExistingAdmin_createAdminIfNotExists() {
        CreateUserDto dto = new CreateUserDto("admin@test.com", "password123");
        UserEntity existing = createUser(20L, "admin@test.com");
        given(userRepository.findByEmail("admin@test.com")).willReturn(Optional.of(existing));

        UserEntity result = userService.createAdminIfNotExists(dto);

        assertThat(result).isEqualTo(existing);
        verify(userRepository).findByEmail("admin@test.com");
    }

    @DisplayName("Should create admin when admin does not exist")
    @Test
    void shouldCreateAdmin_createAdminIfNotExists() {
        CreateUserDto dto = new CreateUserDto("admin@test.com", "password123");
        UserEntity saved = createUser(21L, "admin@test.com");
        RoleEntity adminRole = new RoleEntity("ADMIN");

        given(userRepository.findByEmail("admin@test.com")).willReturn(Optional.empty(), Optional.empty(), Optional.empty());
        given(passwordEncoder.encode("password123")).willReturn("encoded-password");
        given(userRepository.save(any(UserEntity.class))).willReturn(saved);
        given(roleService.getRoleByName("ADMIN")).willReturn(adminRole);

        UserEntity result = userService.createAdminIfNotExists(dto);

        assertThat(result).isEqualTo(saved);
        verify(userRoleService).addUserRole(saved, adminRole);
    }

    @DisplayName("Should return existing user when user already exists")
    @Test
    void shouldReturnUserAlreadyExist_createAdminIfNotExists() {
        CreateUserDto dto = new CreateUserDto("admin@test.com", "password123");
        UserEntity existing = createUser(30L, "admin@test.com");

        given(userRepository.findByEmail("admin@test.com")).willReturn(Optional.of(existing));

        UserEntity result = userService.createAdminIfNotExists(dto);

        assertThat(result).isEqualTo(existing);
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(userRepository).findByEmail("admin@test.com");
    }
}
