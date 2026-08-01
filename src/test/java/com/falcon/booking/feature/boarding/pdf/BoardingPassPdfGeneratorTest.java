package com.falcon.booking.feature.boarding.pdf;

import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.common.qr.QrCodeService;
import com.falcon.booking.feature.boarding.assembler.BoardingPassViewAssembler;
import com.falcon.booking.feature.boarding.dto.BoardingPassDocumentData;
import com.falcon.booking.feature.boarding.dto.BoardingPassView;
import com.falcon.booking.persistence.entity.AirplaneTypeEntity;
import com.falcon.booking.persistence.entity.AirportEntity;
import com.falcon.booking.persistence.entity.BoardingPassEntity;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.entity.RouteEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardingPassPdfGeneratorTest {

    @Mock
    private QrCodeService qrCodeService;
    @Mock
    private BoardingPassViewAssembler boardingPassViewAssembler;
    @Mock
    private BoardingPassPdfService boardingPassPdfService;

    private BoardingPassPdfGenerator boardingPassPdfGenerator;

    @BeforeEach
    void setUp() {
        boardingPassPdfGenerator = new BoardingPassPdfGenerator(qrCodeService, boardingPassViewAssembler, boardingPassPdfService);
        ReflectionTestUtils.setField(boardingPassPdfGenerator, "baseUrl", "https://falcon.example.com");
        ReflectionTestUtils.setField(boardingPassPdfGenerator, "contextPath", "/api");
        ReflectionTestUtils.setField(boardingPassPdfGenerator, "validationPath", "/v1/boarding-passes");
        ReflectionTestUtils.setField(boardingPassPdfGenerator, "minutesBeforeToEndBoarding", 15);
        ReflectionTestUtils.setField(boardingPassPdfGenerator, "minutesBeforeToStartBoarding", 60);
    }

    private PassengerReservationEntity buildPassengerReservation() {
        AirportEntity origin = new AirportEntity();
        origin.setIataCode("BOG");
        origin.setTimezone("America/Bogota");

        AirportEntity destination = new AirportEntity();
        destination.setIataCode("MDE");

        RouteEntity route = new RouteEntity();
        route.setAirportOrigin(origin);
        route.setAirportDestination(destination);
        route.setFlightNumber("AV1234");

        AirplaneTypeEntity airplaneType = new AirplaneTypeEntity();
        airplaneType.configureSeats(108, 12, "ABCDEF");

        FlightEntity flight = new FlightEntity();
        flight.setId(1L);
        flight.setRoute(route);
        flight.setAirplaneType(airplaneType);
        flight.setDepartureDateTime(OffsetDateTime.of(2026, 8, 1, 15, 0, 0, 0, java.time.ZoneOffset.ofHours(-5)));

        PassengerEntity passenger = new PassengerEntity();
        passenger.setId(1L);
        passenger.setFirstName("Ana");
        passenger.setLastName("Perez");
        passenger.setIdentificationNumber("12345");

        ReservationEntity reservation = new ReservationEntity("RES123", flight, "test@test.com", Instant.now());

        return new PassengerReservationEntity(passenger, reservation, 5, SeatClass.ECONOMY);
    }

    @DisplayName("Should build QR validation URL, document and generate PDF")
    @Test
    void shouldGeneratePdfAndDocument() {
        PassengerReservationEntity pr = buildPassengerReservation();
        BoardingPassEntity boardingPass = new BoardingPassEntity(pr, UUID.randomUUID());
        byte[] qr = {4, 5, 6};
        byte[] expectedPdf = {7, 8, 9};
        BoardingPassView mockView = mock(BoardingPassView.class);

        when(qrCodeService.generate(any(String.class))).thenReturn(qr);
        when(boardingPassViewAssembler.toView(any(BoardingPassDocumentData.class))).thenReturn(mockView);
        when(boardingPassPdfService.generate(mockView)).thenReturn(expectedPdf);

        BoardingPassPdfResult result = boardingPassPdfGenerator.generate(boardingPass);

        assertArrayEquals(expectedPdf, result.pdf());

        BoardingPassDocumentData document = result.document();
        assertEquals("ANA PEREZ", document.passengerName());
        assertEquals("AV1234", document.flightNumber());
        assertEquals("BOG", document.originAirport());
        assertEquals("MDE", document.destinationAirport());
        assertEquals("RES123", document.reservationNumber());
        assertArrayEquals(qr, document.qr());
        assertEquals(pr.getFlight().getAirplaneType().getSeatLabel(5), document.seat());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(qrCodeService).generate(urlCaptor.capture());
        assertEquals("https://falcon.example.com/api/v1/boarding-passes/" + boardingPass.getQrToken(), urlCaptor.getValue());

        verify(boardingPassPdfService).generate(mockView);
    }
}
