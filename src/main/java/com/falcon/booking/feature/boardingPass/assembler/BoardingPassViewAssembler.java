package com.falcon.booking.feature.boardingPass.assembler;

import com.falcon.booking.feature.boardingPass.dto.BoardingPassDocumentData;
import com.falcon.booking.feature.boardingPass.dto.BoardingPassView;
import com.falcon.booking.feature.boardingPass.dto.DateTimeView;
import com.falcon.booking.feature.boardingPass.resources.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Slf4j
@Component
public class BoardingPassViewAssembler {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final ResourceService resourceService;

    public BoardingPassViewAssembler(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public BoardingPassView toView(BoardingPassDocumentData document) {
        log.debug("Assembling BoardingPassView for reservation: {}", document.reservationNumber());

        BoardingPassView view = new BoardingPassView(
                document.passengerName(),
                document.flightNumber(),
                document.originAirport(),
                document.destinationAirport(),
                new DateTimeView(document.departureTime().format(DATE), document.departureTime().format(TIME), document.zone()),
                new DateTimeView(document.boardingStartTime().format(DATE), document.boardingStartTime().format(TIME), document.zone()),
                new DateTimeView(document.boardingEndTime().format(DATE), document.boardingEndTime().format(TIME), document.zone()),
                document.seat(),
                document.reservationNumber(),
                asImage(document.qr()),
                asImage(resourceService.loadAsBytes("static/images/falcon-logo.jpg"))
        );
        log.trace("Successfully assembled BoardingPassView for reservation: {}", document.reservationNumber());
        return view;
    }

    private String asImage(byte[] image) {
        return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(image);
    }

}
