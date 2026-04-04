package com.falcon.booking.security.service;

import com.falcon.booking.domain.exception.User.UserNotFoundException;
import com.falcon.booking.domain.service.UserService;
import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.entity.UserRoleEntity;
import com.falcon.booking.security.model.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @DisplayName("Should return UserDetails when user exists")
    @Test
    void shouldReturnUserDetails_loadUserByUsername() {
        RoleEntity role = new RoleEntity("ADMIN");

        UserEntity user = new UserEntity();
        user.setId(10L);
        user.setEmail("admin@test.com");
        user.setPassword("encoded-password");

        UserRoleEntity userRole = new UserRoleEntity();
        setField(userRole, "user", user);
        setField(userRole, "role", role);
        user.setUserRoles(Set.of(userRole));

        given(userService.getUserByEmail("admin@test.com")).willReturn(user);

        UserDetails result = userDetailsService.loadUserByUsername("admin@test.com");

        assertThat(result).isInstanceOf(CustomUserDetails.class);
        assertThat(result.getUsername()).isEqualTo("admin@test.com");
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ADMIN");
        verify(userService).getUserByEmail("admin@test.com");
    }

    @DisplayName("Should throw UsernameNotFoundException when user does not exist")
    @Test
    void shouldThrowException_loadUserByUsername() {
        given(userService.getUserByEmail("ghost@test.com"))
                .willThrow(new UserNotFoundException("ghost@test.com"));

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("ghost@test.com"));

        assertThat(exception.getMessage()).isEqualTo("User not found: ghost@test.com");
        verify(userService).getUserByEmail("ghost@test.com");
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
