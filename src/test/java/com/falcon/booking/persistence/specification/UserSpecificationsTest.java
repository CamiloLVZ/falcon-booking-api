package com.falcon.booking.persistence.specification;

import com.falcon.booking.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

class UserSpecificationsTest {

    @Test
    void hasEmailLike_shouldReturnConjunctionWhenNull() {
        Specification<UserEntity> spec = UserSpecifications.hasEmailLike(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasEmailLike_shouldReturnConjunctionWhenBlank() {
        Specification<UserEntity> spec = UserSpecifications.hasEmailLike("   ");
        assertThat(spec).isNotNull();
    }

    @Test
    void hasDisabled_shouldReturnConjunctionWhenNull() {
        Specification<UserEntity> spec = UserSpecifications.hasDisabled(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasRoleName_shouldReturnConjunctionWhenNull() {
        Specification<UserEntity> spec = UserSpecifications.hasRoleName(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasRoleName_shouldReturnConjunctionWhenBlank() {
        Specification<UserEntity> spec = UserSpecifications.hasRoleName("   ");
        assertThat(spec).isNotNull();
    }
}
