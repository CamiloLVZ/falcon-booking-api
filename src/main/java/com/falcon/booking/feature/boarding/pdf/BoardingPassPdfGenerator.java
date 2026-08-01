package com.falcon.booking.feature.boarding.pdf;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;

@Component
public class BoardingPassPdfGenerator {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${app.boarding-pass.validation-path:/v1/boarding-passes}")
    private String validationPath;

    @Value("${app.flight.boarding.minutes-before-to-close}")
    private int minutesBeforeToEndBoarding;

    @Value("${app.flight.boarding.minutes-before-to-start}")
    private int minutesBeforeToStartBoarding;

    private final QrCodeService qrCodeService;
    private final BoardingPassViewAssembler boardingPassViewAssembler;
    private final BoardingPassPdfService boardingPassPdfService;

    public BoardingPassPdfGenerator(QrCodeService qrCodeService,
                                    BoardingPassViewAssembler boardingPassViewAssembler,
                                    BoardingPassPdfService boardingPassPdfService) {
        this.qrCodeService = qrCodeService;
        this.boardingPassViewAssembler = boardingPassViewAssembler;
        this.boardingPassPdfService = boardingPassPdfService;
    }

    public BoardingPassPdfResult generate(BoardingPassEntity boardingPass) {
        PassengerReservationEntity passengerReservation = boardingPass.getPassengerReservation();
        byte[] qr = qrCodeService.generate(buildValidationUrl(boardingPass));
        BoardingPassDocumentData document = buildDocument(passengerReservation, qr);
        BoardingPassView view = boardingPassViewAssembler.toView(document);
        byte[] pdf = boardingPassPdfService.generate(view);
        return new BoardingPassPdfResult(document, pdf);
    }

    private String buildValidationUrl(BoardingPassEntity boardingPass) {
        return baseUrl + contextPath + validationPath + "/" + boardingPass.getQrToken();
    }

    private BoardingPassDocumentData buildDocument(PassengerReservationEntity passengerReservation, byte[] qr) {
        ReservationEntity reservation = passengerReservation.getReservation();
        PassengerEntity passenger = passengerReservation.getPassenger();
        FlightEntity flight = passengerReservation.getFlight();

        OffsetDateTime departureDateTime = flight.getDepartureDateTime();

        LocalDateTime departureLocalDateTime = departureDateTime.atZoneSameInstant(ZoneId.of(flight.getRoute().getAirportOrigin().getTimezone())).toLocalDateTime();
        LocalDateTime boardingStartTime = departureLocalDateTime.minusMinutes(minutesBeforeToStartBoarding);
        LocalDateTime boardingEndTime = departureLocalDateTime.minusMinutes(minutesBeforeToEndBoarding);
        RouteEntity route = flight.getRoute();
        AirportEntity airportOrigin = route.getAirportOrigin();
        AirplaneTypeEntity airplaneType = flight.getAirplaneType();

        String seatLabel = airplaneType.getSeatLabel(passengerReservation.getSeatNumber());
        String zone = ZoneId.of(airportOrigin.getTimezone()).getDisplayName(TextStyle.SHORT, Locale.US);

        return new BoardingPassDocumentData(
                passenger.getFullName(),
                route.getFlightNumber(),
                route.getAirportOrigin().getIataCode(),
                route.getAirportDestination().getIataCode(),
                departureLocalDateTime,
                boardingStartTime,
                boardingEndTime,
                zone,
                seatLabel,
                reservation.getNumber(),
                qr
        );
    }
}
