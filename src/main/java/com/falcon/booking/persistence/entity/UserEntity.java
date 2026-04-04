package com.falcon.booking.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(nullable = false, length = 256)
    private String password;

    @Column(nullable = false)
    private Boolean disabled;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    protected Set<UserRoleEntity> userRoles = new HashSet<>();

    public Set<RoleEntity> getRoles(){

        return Optional.ofNullable(userRoles)
                .orElse(Collections.emptySet())
                .stream()
                .map(UserRoleEntity::getRole)
                .collect(Collectors.toSet());
    }

    public void addRole(RoleEntity role){
        this.getRoles().add(role);
    }

}
