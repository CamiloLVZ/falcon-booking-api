package com.falcon.booking.feature.auth.service;

import com.falcon.booking.feature.auth.exception.RoleNotFoundException;
import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public RoleEntity getRoleByName(String roleName){
        return roleRepository.findByName(roleName)
                .orElseThrow(()->new RoleNotFoundException(roleName));
    }

}
