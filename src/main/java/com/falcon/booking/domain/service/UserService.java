package com.falcon.booking.domain.service;

import com.falcon.booking.domain.exception.User.UserAlreadyExistException;
import com.falcon.booking.domain.exception.User.UserNotFoundException;
import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.repository.UserRepository;
import com.falcon.booking.web.dto.user.CreateUserDto;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

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

    public UserEntity buildUser(CreateUserDto createUserDto){
        if(userRepository.findByEmail(createUserDto.email()).isPresent())
            throw new UserAlreadyExistException(createUserDto.email());
        UserEntity user = new UserEntity();
        user.setEmail(createUserDto.email());
        user.setPassword(passwordEncoder.encode(createUserDto.password()));
        user.setDisabled(false);
        return user;
    }

    @Transactional
    public UserEntity createClientUser(CreateUserDto createUserDto){
        if(userRepository.findByEmail(createUserDto.email()).isPresent())
            throw new UserAlreadyExistException(createUserDto.email());
        UserEntity user = userRepository.save(buildUser(createUserDto));
        RoleEntity role = roleService.getRoleByName("CLIENT");
        userRoleService.addUserRole(user, role);
        logger.debug("Client user created successfully: {}", user.getEmail());
        return user;
    }

    @Transactional
    public UserEntity createAdminUser(CreateUserDto createUserDto){
        if(userRepository.findByEmail(createUserDto.email()).isPresent())
            throw new UserAlreadyExistException(createUserDto.email());
        UserEntity user = userRepository.save(buildUser(createUserDto));
        RoleEntity role = roleService.getRoleByName("ADMIN");
        userRoleService.addUserRole(user, role);
        logger.info("Admin user created successfully: {}", user.getEmail());
        return user;
    }

    @Transactional
    public UserEntity createAdminIfNotExists(CreateUserDto dto){
        try {
            return userRepository.findByEmail(dto.email()).orElseGet(() ->{
                    logger.info("Initializing admin user");
                    return createAdminUser(dto);
            });
        } catch (UserAlreadyExistException | DataIntegrityViolationException e) {
            return getUserByEmail(dto.email());
        }
    }
}
