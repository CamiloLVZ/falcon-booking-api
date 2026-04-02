package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<UserEntity> findByEmail(String email);
}
