package com.falcon.booking.security.evaluator;

import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.repository.ReservationRepository;
import com.falcon.booking.security.jwt.JwtPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReservationSecurityEvaluatorTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationSecurityEvaluator evaluator;

    private Authentication createAdminAuthentication() {
        JwtPayload payload = new JwtPayload(1L, "admin@test.com", List.of("ADMIN"));
        return new UsernamePasswordAuthenticationToken(payload, null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private Authentication createClientAuthentication(Long userId) {
        JwtPayload payload = new JwtPayload(userId, "client@test.com", List.of("CLIENT"));
        return new UsernamePasswordAuthenticationToken(payload, null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
    }

    @DisplayName("Should return true when user is admin")
    @Test
    void shouldReturnTrue_whenUserIsAdmin() {
        Authentication authentication = createAdminAuthentication();

        boolean result = evaluator.isReservationOwnerOrAdmin("ABC123", authentication);

        assertThat(result).isTrue();
    }

    @DisplayName("Should return true when user owns the reservation")
    @Test
    void shouldReturnTrue_whenUserOwnsReservation() {
        Authentication authentication = createClientAuthentication(1L);
        UserEntity user = new UserEntity();
        user.setId(1L);
        ReservationEntity reservation = new ReservationEntity("ABC123", null, "mail@test.com", Instant.now());
        reservation.setUser(user);

        given(reservationRepository.findByNumber("ABC123")).willReturn(Optional.of(reservation));

        boolean result = evaluator.isReservationOwnerOrAdmin("ABC123", authentication);

        assertThat(result).isTrue();
    }

    @DisplayName("Should return false when user is not the owner")
    @Test
    void shouldReturnFalse_whenUserIsNotOwner() {
        Authentication authentication = createClientAuthentication(2L);
        UserEntity user = new UserEntity();
        user.setId(1L);
        ReservationEntity reservation = new ReservationEntity("ABC123", null, "mail@test.com", Instant.now());
        reservation.setUser(user);

        given(reservationRepository.findByNumber("ABC123")).willReturn(Optional.of(reservation));

        boolean result = evaluator.isReservationOwnerOrAdmin("ABC123", authentication);

        assertThat(result).isFalse();
    }

    @DisplayName("Should return true when reservation does not exist")
    @Test
    void shouldReturnTrue_whenReservationNotFound() {
        Authentication authentication = createClientAuthentication(1L);

        given(reservationRepository.findByNumber("UNKNOWN")).willReturn(Optional.empty());

        boolean result = evaluator.isReservationOwnerOrAdmin("UNKNOWN", authentication);

        assertThat(result).isTrue();
    }

    @DisplayName("Should return false when authentication is null")
    @Test
    void shouldReturnFalse_whenAuthenticationIsNull() {
        boolean result = evaluator.isReservationOwnerOrAdmin("ABC123", null);

        assertThat(result).isFalse();
    }

    @DisplayName("Should return false when principal is not JwtPayload")
    @Test
    void shouldReturnFalse_whenPrincipalIsNotJwtPayload() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("not-a-payload", null);

        boolean result = evaluator.isReservationOwnerOrAdmin("ABC123", authentication);

        assertThat(result).isFalse();
    }

    @DisplayName("Should return false when reservation has no user linked")
    @Test
    void shouldReturnFalse_whenReservationHasNoUser() {
        Authentication authentication = createClientAuthentication(1L);
        ReservationEntity reservation = new ReservationEntity("ABC123", null, "mail@test.com", Instant.now());

        given(reservationRepository.findByNumber("ABC123")).willReturn(Optional.of(reservation));

        boolean result = evaluator.isReservationOwnerOrAdmin("ABC123", authentication);

        assertThat(result).isFalse();
    }
}