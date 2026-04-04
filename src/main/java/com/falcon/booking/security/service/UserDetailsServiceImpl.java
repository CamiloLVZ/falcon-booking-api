package com.falcon.booking.security.service;

import com.falcon.booking.domain.exception.User.UserNotFoundException;
import com.falcon.booking.domain.service.UserService;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.security.model.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            UserEntity userEntity = userService.getUserByEmail(email);
            List<GrantedAuthority> authorities = getGrantedAuthorities(getRoles(userEntity));
            return new CustomUserDetails(
                    userEntity.getId(),
                    userEntity.getEmail(),
                    userEntity.getPassword(),
                    authorities);

        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException("User not found: " + email);
        }
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> roles) {
        List<GrantedAuthority> authorities =  new ArrayList<>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        return authorities;
    }

    private List<String> getRoles(UserEntity user) {
        return user.getUserRoles()
                .stream()
                .map((userRole)-> userRole.getRole().getName())
                .toList();
    }

}
