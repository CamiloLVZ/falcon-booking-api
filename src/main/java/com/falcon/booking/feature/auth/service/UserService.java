package com.falcon.booking.feature.auth.service;

import com.falcon.booking.feature.auth.dto.CreateUserDto;
import com.falcon.booking.feature.auth.exception.UserAlreadyExistException;
import com.falcon.booking.feature.auth.exception.UserNotFoundException;
import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final UserRoleService userRoleService;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleService roleService, UserRoleService userRoleService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
        this.userRoleService = userRoleService;
    }

    public UserEntity getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(()->new UserNotFoundException(email));
    }

    private void checkUserDoesNotExistByEmail(String email) {
        if (userRepository.findByEmail(email).isPresent())
            throw new UserAlreadyExistException(email);
    }

    public UserEntity buildUser(CreateUserDto createUserDto){
        checkUserDoesNotExistByEmail(createUserDto.email());

        UserEntity user = new UserEntity();
        user.setEmail(createUserDto.email());
        user.setPassword(passwordEncoder.encode(createUserDto.password()));
        user.setDisabled(false);
        return user;
    }

    @Transactional
    public UserEntity createClientUser(CreateUserDto createUserDto){
        UserEntity user = userRepository.save(buildUser(createUserDto));
        RoleEntity role = roleService.getRoleByName("CLIENT");
        userRoleService.addUserRole(user, role);
        log.debug("Client user created successfully: {}", user.getEmail());
        return user;
    }

    @Transactional
    public UserEntity createAdminUser(CreateUserDto createUserDto){
        UserEntity user = userRepository.save(buildUser(createUserDto));
        RoleEntity role = roleService.getRoleByName("ADMIN");
        userRoleService.addUserRole(user, role);
        log.info("Admin user created successfully: {}", user.getEmail());
        return user;
    }

    @Transactional
    public UserEntity createAdminIfNotExists(CreateUserDto dto){
        try {
            return userRepository.findByEmail(dto.email()).orElseGet(() ->{
                    log.info("Initializing admin user");
                    return createAdminUser(dto);
            });
        } catch (UserAlreadyExistException | DataIntegrityViolationException e) {
            return getUserByEmail(dto.email());
        }
    }
}
