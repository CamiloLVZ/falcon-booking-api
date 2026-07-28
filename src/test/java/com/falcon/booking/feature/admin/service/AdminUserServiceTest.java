package com.falcon.booking.feature.admin.service;

import com.falcon.booking.feature.admin.dto.AdminUserDto;
import com.falcon.booking.feature.admin.dto.UpdateUserCredentialsDto;
import com.falcon.booking.feature.auth.exception.UserAlreadyExistException;
import com.falcon.booking.feature.auth.exception.UserNotFoundException;
import com.falcon.booking.feature.passenger.dto.ResponsePassengerDto;
import com.falcon.booking.feature.passenger.mapper.PassengerMapper;
import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.entity.UserRoleEntity;
import com.falcon.booking.persistence.repository.UserRepository;
import com.falcon.booking.persistence.specification.UserSpecifications;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PassengerMapper passengerMapper;
    @InjectMocks
    private AdminUserService adminUserService;
    @Captor
    private ArgumentCaptor<UserEntity> userCaptor;

    private UserEntity createUser(Long id, String email, boolean disabled, String roleName) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setDisabled(disabled);
        RoleEntity role = new RoleEntity(roleName);
        UserRoleEntity userRole = new UserRoleEntity(user, role);
        user.getUserRoles().add(userRole);
        return user;
    }

    @DisplayName("Should return paginated users when no filters provided")
    @Test
    void shouldReturnAllUsers_noFilters() {
        UserEntity user = createUser(1L, "user@test.com", false, "CLIENT");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());
        Page<UserEntity> userPage = new PageImpl<>(List.of(user), pageable, 1);
        given(userRepository.findAll(any(Specification.class), eq(pageable))).willReturn(userPage);

        Page<AdminUserDto> result = adminUserService.getAllUsers(null, null, null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).email()).isEqualTo("user@test.com");
        assertThat(result.getContent().get(0).disabled()).isFalse();
        assertThat(result.getContent().get(0).roles()).contains("CLIENT");
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @DisplayName("Should filter users by email")
    @Test
    void shouldFilterUsersByEmail() {
        UserEntity user = createUser(1L, "john@test.com", false, "CLIENT");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());
        Page<UserEntity> userPage = new PageImpl<>(List.of(user), pageable, 1);
        given(userRepository.findAll(any(Specification.class), eq(pageable))).willReturn(userPage);

        Page<AdminUserDto> result = adminUserService.getAllUsers("john", null, null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).email()).isEqualTo("john@test.com");
    }

    @DisplayName("Should filter users by disabled status")
    @Test
    void shouldFilterUsersByDisabled() {
        UserEntity user = createUser(1L, "user@test.com", true, "CLIENT");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());
        Page<UserEntity> userPage = new PageImpl<>(List.of(user), pageable, 1);
        given(userRepository.findAll(any(Specification.class), eq(pageable))).willReturn(userPage);

        Page<AdminUserDto> result = adminUserService.getAllUsers(null, true, null, 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).disabled()).isTrue();
    }

    @DisplayName("Should filter users by role name")
    @Test
    void shouldFilterUsersByRole() {
        UserEntity user = createUser(1L, "admin@test.com", false, "ADMIN");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());
        Page<UserEntity> userPage = new PageImpl<>(List.of(user), pageable, 1);
        given(userRepository.findAll(any(Specification.class), eq(pageable))).willReturn(userPage);

        Page<AdminUserDto> result = adminUserService.getAllUsers(null, null, "ADMIN", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).roles()).contains("ADMIN");
    }

    @DisplayName("Should return empty page when no users match filters")
    @Test
    void shouldReturnEmptyPage_noMatch() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());
        Page<UserEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        given(userRepository.findAll(any(Specification.class), eq(pageable))).willReturn(emptyPage);

        Page<AdminUserDto> result = adminUserService.getAllUsers("nonexistent", null, null, 0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @DisplayName("Should include passenger profile when user has one")
    @Test
    void shouldIncludePassengerProfile() {
        UserEntity user = createUser(1L, "user@test.com", false, "CLIENT");
        user.setPassengerProfile(new com.falcon.booking.persistence.entity.PassengerEntity());
        ResponsePassengerDto passengerDto = new ResponsePassengerDto(1L, "Juan", "Perez", null, "CO", null, null, "12345");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());
        Page<UserEntity> userPage = new PageImpl<>(List.of(user), pageable, 1);
        given(userRepository.findAll(any(Specification.class), eq(pageable))).willReturn(userPage);
        given(passengerMapper.toResponseDto(user.getPassengerProfile())).willReturn(passengerDto);

        Page<AdminUserDto> result = adminUserService.getAllUsers(null, null, null, 0, 10);

        assertThat(result.getContent().get(0).passengerProfile()).isNotNull();
        assertThat(result.getContent().get(0).passengerProfile().firstName()).isEqualTo("Juan");
    }

    @DisplayName("Should return null passenger profile when user has none")
    @Test
    void shouldReturnNullPassengerProfile() {
        UserEntity user = createUser(1L, "user@test.com", false, "CLIENT");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("email").ascending());
        Page<UserEntity> userPage = new PageImpl<>(List.of(user), pageable, 1);
        given(userRepository.findAll(any(Specification.class), eq(pageable))).willReturn(userPage);

        Page<AdminUserDto> result = adminUserService.getAllUsers(null, null, null, 0, 10);

        assertThat(result.getContent().get(0).passengerProfile()).isNull();
        verify(passengerMapper, never()).toResponseDto(any());
    }

    @DisplayName("Should update user email")
    @Test
    void shouldUpdateUserEmail() {
        UserEntity user = createUser(1L, "old@test.com", false, "CLIENT");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.findByEmail("new@test.com")).willReturn(Optional.empty());

        adminUserService.updateUserCredentials(1L, new UpdateUserCredentialsDto("new@test.com", null));

        verify(userRepository).save(userCaptor.capture());
        UserEntity saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("new@test.com");
    }

    @DisplayName("Should update user password")
    @Test
    void shouldUpdateUserPassword() {
        UserEntity user = createUser(1L, "user@test.com", false, "CLIENT");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.encode("newPass123")).willReturn("encoded-new-pass");

        adminUserService.updateUserCredentials(1L, new UpdateUserCredentialsDto(null, "newPass123"));

        verify(userRepository).save(userCaptor.capture());
        UserEntity saved = userCaptor.getValue();
        assertThat(saved.getPassword()).isEqualTo("encoded-new-pass");
    }

    @DisplayName("Should update both email and password")
    @Test
    void shouldUpdateBothEmailAndPassword() {
        UserEntity user = createUser(1L, "old@test.com", false, "CLIENT");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.findByEmail("new@test.com")).willReturn(Optional.empty());
        given(passwordEncoder.encode("newPass123")).willReturn("encoded-new-pass");

        adminUserService.updateUserCredentials(1L, new UpdateUserCredentialsDto("new@test.com", "newPass123"));

        verify(userRepository).save(userCaptor.capture());
        UserEntity saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo("new@test.com");
        assertThat(saved.getPassword()).isEqualTo("encoded-new-pass");
    }

    @DisplayName("Should throw exception when user does not exist")
    @Test
    void shouldThrowException_userNotFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> adminUserService.updateUserCredentials(99L, new UpdateUserCredentialsDto("new@test.com", null)));
    }

    @DisplayName("Should throw exception when no fields provided")
    @Test
    void shouldThrowException_noFields() {
        UserEntity user = createUser(1L, "user@test.com", false, "CLIENT");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> adminUserService.updateUserCredentials(1L, new UpdateUserCredentialsDto(null, null)));
    }

    @DisplayName("Should throw exception when email already in use by another user")
    @Test
    void shouldThrowException_emailAlreadyInUse() {
        UserEntity user = createUser(1L, "user@test.com", false, "CLIENT");
        UserEntity otherUser = createUser(2L, "existing@test.com", false, "CLIENT");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.findByEmail("existing@test.com")).willReturn(Optional.of(otherUser));

        UpdateUserCredentialsDto dto =
                new UpdateUserCredentialsDto("existing@test.com", null);

        assertThrows(
                UserAlreadyExistException.class,
                () -> adminUserService.updateUserCredentials(1L, dto)
        );
    }

    @DisplayName("Should allow updating email to same email")
    @Test
    void shouldAllowSameEmail() {
        UserEntity user = createUser(1L, "same@test.com", false, "CLIENT");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.findByEmail("same@test.com")).willReturn(Optional.of(user));

        adminUserService.updateUserCredentials(1L, new UpdateUserCredentialsDto("same@test.com", null));

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("same@test.com");
    }

    @DisplayName("Should toggle disabled from false to true")
    @Test
    void shouldToggleDisabledFalseToTrue() {
        UserEntity user = createUser(1L, "user@test.com", false, "CLIENT");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        adminUserService.toggleUserDisabled(1L);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getDisabled()).isTrue();
    }

    @DisplayName("Should toggle disabled from true to false")
    @Test
    void shouldToggleDisabledTrueToFalse() {
        UserEntity user = createUser(1L, "user@test.com", true, "CLIENT");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        adminUserService.toggleUserDisabled(1L);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getDisabled()).isFalse();
    }

    @DisplayName("Should throw exception when toggling disabled for non-existent user")
    @Test
    void shouldThrowException_toggleUserNotFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> adminUserService.toggleUserDisabled(99L));
    }
}
