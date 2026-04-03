package com.falcon.booking.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "user_roles")
@Getter
@NoArgsConstructor
public class UserRoleEntity {

    @EmbeddedId
    private UserRoleId id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    @MapsId("userId")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "id_role", nullable = false)
    @MapsId("roleId")
    private RoleEntity role;

    public UserRoleEntity(UserEntity user, RoleEntity role) {
        this.id = new UserRoleId(user.getId(), role.getId());
        this.user = user;
        this.role = role;
        user.userRoles.add(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserRoleEntity that = (UserRoleEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
