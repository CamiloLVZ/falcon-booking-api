package com.falcon.booking.domain.service;

import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.entity.UserRoleEntity;
import com.falcon.booking.persistence.repository.UserRoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserRoleService {


    private final UserRoleRepository userRoleRepository;

    @Autowired
    public UserRoleService(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    @Transactional
    public UserRoleEntity addUserRole(UserEntity user, RoleEntity role){

        Optional<UserRoleEntity> existing = userRoleRepository.findByUserAndRole(user, role);

        if (existing.isPresent()) {
            return existing.get();
        }

        UserRoleEntity newUserRole = new UserRoleEntity(user, role);
        user.getUserRoles().add(newUserRole);

        return userRoleRepository.save(newUserRole);
    }
}
