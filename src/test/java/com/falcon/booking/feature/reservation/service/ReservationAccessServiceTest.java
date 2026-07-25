package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.feature.reservation.exception.InvalidReservationAccessException;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationAccessServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationAccessService reservationAccessService;

    @Test
    void shouldReturnReservationWhenNumberAndContactEmailMatch() {
        ReservationEntity reservation = new ReservationEntity();
        when(reservationRepository.findByNumberAndContactEmail("ABC123", "contact@test.com"))
                .thenReturn(Optional.of(reservation));

        ReservationEntity result = reservationAccessService.getReservationByNumberAndContactEmail(" abc123 ", " Contact@Test.Com ");

        assertThat(result).isSameAs(reservation);
    }

    @Test
    void shouldRejectAccessWhenNumberAndContactEmailDoNotMatch() {
        when(reservationRepository.findByNumberAndContactEmail("ABC123", "contact@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationAccessService.getReservationByNumberAndContactEmail("ABC123", "contact@test.com"))
                .isInstanceOf(InvalidReservationAccessException.class);
    }
}
