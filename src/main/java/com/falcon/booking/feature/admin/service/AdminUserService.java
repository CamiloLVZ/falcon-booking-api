package com.falcon.booking.feature.admin.service;

import com.falcon.booking.feature.admin.dto.AdminUserDto;
import com.falcon.booking.feature.admin.dto.UpdateUserCredentialsDto;
import com.falcon.booking.feature.auth.exception.UserAlreadyExistException;
import com.falcon.booking.feature.auth.exception.UserNotFoundException;
import com.falcon.booking.feature.auth.service.UserService;
import com.falcon.booking.feature.passenger.mapper.PassengerMapper;
import com.falcon.booking.feature.passenger.dto.ResponsePassengerDto;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.repository.UserRepository;
import com.falcon.booking.persistence.specification.UserSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PassengerMapper passengerMapper;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder, PassengerMapper passengerMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passengerMapper = passengerMapper;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserDto> getAllUsers(String email, Boolean disabled, String role, int page, int size) {
        Specification<UserEntity> spec = Specification.allOf();
        spec = spec.and(UserSpecifications.hasEmailLike(email));
        spec = spec.and(UserSpecifications.hasDisabled(disabled));
        spec = spec.and(UserSpecifications.hasRoleName(role));

        Pageable pageable = PageRequest.of(page, size, Sort.by("email").ascending());
        return userRepository.findAll(spec, pageable).map(this::toAdminUserDto);
    }

    @Transactional
    public void updateUserCredentials(Long userId, UpdateUserCredentialsDto dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(userId)));

        boolean updated = false;

        if (dto.email() != null && !dto.email().isBlank()) {
            Optional<UserEntity> existingWithEmail = userRepository.findByEmail(dto.email());
            if (existingWithEmail.isPresent() && !existingWithEmail.get().getId().equals(userId)) {
                throw new UserAlreadyExistException(dto.email());
            }
            user.setEmail(dto.email());
            updated = true;
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
            updated = true;
        }

        if (!updated) {
            throw new IllegalArgumentException("At least one of email or password must be provided");
        }

        userRepository.save(user);
    }

    private AdminUserDto toAdminUserDto(UserEntity user) {
        ResponsePassengerDto passengerProfile = null;
        if (user.getPassengerProfile() != null) {
            passengerProfile = passengerMapper.toResponseDto(user.getPassengerProfile());
        }

        return new AdminUserDto(
                user.getId(),
                user.getEmail(),
                user.getDisabled(),
                user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()),
                passengerProfile
        );
    }
}
