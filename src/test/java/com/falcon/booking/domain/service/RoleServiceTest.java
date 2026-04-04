package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.User.RoleNotFoundException;
import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.repository.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    @DisplayName("Should return role when role exists")
    @Test
    void shouldReturnRole_getRoleByName() {
        RoleEntity role = new RoleEntity("ADMIN");
        given(roleRepository.findByName("ADMIN")).willReturn(Optional.of(role));

        RoleEntity result = roleService.getRoleByName("ADMIN");

        assertThat(result).isEqualTo(role);
        verify(roleRepository).findByName("ADMIN");
    }

    @DisplayName("Should throw exception when role does not exist")
    @Test
    void shouldThrowException_getRoleByName() {
        given(roleRepository.findByName("MANAGER")).willReturn(Optional.empty());

        RoleNotFoundException ex = assertThrows(RoleNotFoundException.class,
                () -> roleService.getRoleByName("MANAGER"));

        assertThat(ex.getMessage()).contains("MANAGER");
        verify(roleRepository).findByName("MANAGER");
    }
}
