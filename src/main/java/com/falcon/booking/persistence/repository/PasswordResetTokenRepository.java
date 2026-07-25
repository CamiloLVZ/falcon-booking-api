package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.PasswordResetTokenEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    Optional<PasswordResetTokenEntity> findByTokenHash(String tokenHash);
    void deleteByUser(UserEntity user);
}
