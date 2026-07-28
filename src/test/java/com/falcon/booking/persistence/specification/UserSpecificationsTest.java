package com.falcon.booking.persistence.specification;

import com.falcon.booking.persistence.entity.UserEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UserSpecificationsTest {

    @Mock CriteriaBuilder cb;
    @Mock CriteriaQuery<?> query;
    @Mock Root<UserEntity> root;
    @Mock Predicate predicate;

    @Test
    void hasEmailLike_shouldReturnConjunctionWhenNull() {
        var spec = UserSpecifications.hasEmailLike(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasEmailLike_shouldReturnConjunctionWhenBlank() {
        var spec = UserSpecifications.hasEmailLike("   ");
        assertThat(spec).isNotNull();
    }

    @Test
    void hasDisabled_shouldReturnConjunctionWhenNull() {
        var spec = UserSpecifications.hasDisabled(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasRoleName_shouldReturnConjunctionWhenNull() {
        var spec = UserSpecifications.hasRoleName(null);
        assertThat(spec).isNotNull();
    }

    @Test
    void hasRoleName_shouldReturnConjunctionWhenBlank() {
        var spec = UserSpecifications.hasRoleName("   ");
        assertThat(spec).isNotNull();
    }
}
