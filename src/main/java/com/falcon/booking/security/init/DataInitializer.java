package com.falcon.booking.security.init;

import com.falcon.booking.domain.service.RoleService;
import com.falcon.booking.domain.service.UserService;
import com.falcon.booking.web.dto.user.CreateUserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final RoleService roleService;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Autowired
    public DataInitializer(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Override
    public void run(String... args) {
        userService.createAdminIfNotExists(new CreateUserDto(adminEmail, adminPassword));
    }
}
