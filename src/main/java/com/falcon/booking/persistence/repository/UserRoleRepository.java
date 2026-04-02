package com.falcon.booking.persistence.repository;

import com.falcon.booking.persistence.entity.RoleEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.entity.UserRoleEntity;
import com.falcon.booking.persistence.entity.UserRoleId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity,Long> {

    @Query("SELECT COUNT(ur) > 0 FROM UserRoleEntity ur WHERE ur.user.email=:email AND ur.role.name = :roleName")
    boolean ExistsByEmailAndRoleName(@Param("email") String userEmail, @Param("roleName") String roleName);
    Optional<UserRoleEntity> findByUserAndRole(UserEntity user, RoleEntity role);
    Optional<UserRoleEntity> findById(UserRoleId id);
}
