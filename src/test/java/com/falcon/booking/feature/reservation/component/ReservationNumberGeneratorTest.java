package com.falcon.booking.feature.reservation.component;

import com.falcon.booking.persistence.repository.ReservationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationNumberGeneratorTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationNumberGenerator reservationNumberGenerator;

    @Test
    @DisplayName("Should generate a unique 6 character reservation number")
    void shouldGenerateUniqueNumber() {
        when(reservationRepository.existsByNumber(anyString())).thenReturn(false);

        String result = reservationNumberGenerator.generate();

        assertNotNull(result);
        assertEquals(6, result.length());
    }

    @Test
    @DisplayName("Should retry if number already exists")
    void shouldRetryIfNumberExists() {
        when(reservationRepository.existsByNumber(anyString()))
                .thenReturn(true)  // first try exists
                .thenReturn(false); // second try unique

        String result = reservationNumberGenerator.generate();

        assertNotNull(result);
        assertEquals(6, result.length());
    }
}
