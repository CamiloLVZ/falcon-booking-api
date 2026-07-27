package com.falcon.booking.security.init;

import com.falcon.booking.feature.auth.dto.CreateUserDto;
import com.falcon.booking.feature.auth.service.RoleService;
import com.falcon.booking.feature.auth.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserService userService;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private DataInitializer dataInitializer;

    @DisplayName("Should create admin user on startup")
    @Test
    void shouldCreateAdminUserOnRun() {
        ReflectionTestUtils.setField(dataInitializer, "adminEmail", "admin@test.com");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "admin123");

        dataInitializer.run();

        verify(userService).createAdminIfNotExists(any(CreateUserDto.class));
    }
}
