package com.falcon.booking.feature.reservation.service;

import com.falcon.booking.common.enums.PassengerReservationStatus;
import com.falcon.booking.common.enums.ReservationStatus;
import com.falcon.booking.feature.passenger.exception.PassengerNotFoundException;
import com.falcon.booking.feature.passenger.service.PassengerService;
import com.falcon.booking.feature.payment.dto.PaymentPassengerDto;
import com.falcon.booking.feature.payment.dto.PaymentRequestDto;
import com.falcon.booking.feature.reservation.component.ReservationNumberGenerator;
import com.falcon.booking.feature.reservation.dto.ResponseReservationDto;
import com.falcon.booking.feature.reservation.exception.PassengerAlreadyReservedFlightException;
import com.falcon.booking.feature.reservation.exception.ReservationCancellationTimeExpiredException;
import com.falcon.booking.feature.reservation.mapper.ReservationMapper;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.entity.UserEntity;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import com.falcon.booking.persistence.repository.ReservationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import com.falcon.booking.common.email.EmailService;
import com.falcon.booking.common.email.dto.EmailInlineImage;
import com.falcon.booking.common.email.dto.EmailRequest;
import com.falcon.booking.common.email.template.EmailTemplateService;
import com.falcon.booking.feature.boarding.resources.ResourceService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ReservationCommandService {

    private static final String LOGO_PATH = "static/images/falcon-logo.jpg";
    private static final String LOGO_CID = "falcon-logo";

    private final ReservationRepository reservationRepository;
    private final PassengerReservationRepository passengerReservationRepository;
    private final PassengerService passengerService;
    private final ReservationMapper reservationMapper;
    private final ReservationQueryService reservationQueryService;
    private final ReservationAccessService reservationAccessService;
    private final ReservationNumberGenerator reservationNumberGenerator;
    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;
    private final byte[] logoBytes;

    @Value("${app.reservation.cancellation.minimum-hours-before-departure}")
    private long minimumHoursBeforeDeparture;

    public ReservationCommandService(ReservationRepository reservationRepository,
                                     PassengerReservationRepository passengerReservationRepository,
                                     PassengerService passengerService,
                                     ReservationMapper reservationMapper,
                                     ReservationQueryService reservationQueryService,
                                     ReservationAccessService reservationAccessService,
                                     ReservationNumberGenerator reservationNumberGenerator,
                                     @Qualifier("resendEmailService") EmailService emailService,
                                     EmailTemplateService emailTemplateService,
                                     ResourceService resourceService) {
        this.reservationRepository = reservationRepository;
        this.passengerReservationRepository = passengerReservationRepository;
        this.passengerService = passengerService;
        this.reservationMapper = reservationMapper;
        this.reservationQueryService = reservationQueryService;
        this.reservationAccessService = reservationAccessService;
        this.reservationNumberGenerator = reservationNumberGenerator;
        this.emailService = emailService;
        this.emailTemplateService = emailTemplateService;
        this.logoBytes = resourceService.loadAsBytes(LOGO_PATH);
    }

    public ReservationEntity createReservationFromPayment(PaymentRequestDto requestDto, FlightEntity flight, UserEntity user) {
        checkPassengersAlreadyReservedFlight(requestDto.passengers(), flight);

        String reservationNumber = reservationNumberGenerator.generate();
        ReservationEntity reservation = reservationRepository.save(new ReservationEntity(reservationNumber, flight, requestDto.contactEmail(), Instant.now()));

        if (user != null) {
            reservation.setUser(user);
        }

        List<PassengerReservationEntity> passengerReservations = new ArrayList<>();
        for (PaymentPassengerDto dto : requestDto.passengers()) {
            PassengerEntity passenger = passengerService.createOrGetPassenger(dto.getPassenger());
            PassengerReservationEntity pr = new PassengerReservationEntity(passenger, reservation, null, dto.getSeatClass());
            pr.setPrice(dto.getUnitPrice());
            passengerReservations.add(pr);
        }
        passengerReservationRepository.saveAll(passengerReservations);

        log.info("Created reservation number {} for flight {} via payment. Passengers: {}", reservation.getNumber(), flight.getId(), passengerReservations.size());
        return reservation;
    }

    @Async("boardingExecutor")
    @Transactional(readOnly = true)
    public void sendReservationConfirmationEmail(Long reservationId) {
        try {
            log.info("Starting Async Reservation Confirmation Email sending for Reservation ID: {}", reservationId);
            ReservationEntity reservation = reservationRepository.findByIdWithPassengers(reservationId)
                    .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));

            FlightEntity flight = reservation.getFlight();
            OffsetDateTime departureDateTime = flight.getDepartureDateTime();
            LocalDateTime departureLocalDateTime = departureDateTime.atZoneSameInstant(ZoneId.of(flight.getRoute().getAirportOrigin().getTimezone())).toLocalDateTime();

            BigDecimal totalAmount = BigDecimal.ZERO;
            List<Map<String, Object>> passengerDataList = new ArrayList<>();
            for (PassengerReservationEntity pr : reservation.getPassengerReservations()) {
                Map<String, Object> pMap = new HashMap<>();
                pMap.put("name", pr.getPassenger().getFullName());
                pMap.put("identification", pr.getPassenger().getIdentification());
                pMap.put("seatClass", pr.getSeatClass().name());
                pMap.put("price", pr.getPrice());
                passengerDataList.add(pMap);
                if (pr.getPrice() != null) {
                    totalAmount = totalAmount.add(pr.getPrice());
                }
            }

            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("reservationNumber", reservation.getNumber());
            templateVariables.put("flightNumber", flight.getRoute().getFlightNumber());
            templateVariables.put("originAirport", flight.getRoute().getAirportOrigin().getIataCode());
            templateVariables.put("destinationAirport", flight.getRoute().getAirportDestination().getIataCode());
            templateVariables.put("departureDate", departureLocalDateTime.toLocalDate().toString());
            templateVariables.put("departureTime", departureLocalDateTime.toLocalTime().toString());
            templateVariables.put("totalAmount", totalAmount);
            templateVariables.put("passengers", passengerDataList);

            String html = emailTemplateService.process("email/reservation-confirmation-email", templateVariables);
            EmailInlineImage logoInline = new EmailInlineImage(LOGO_CID, "image/jpeg", logoBytes);

            EmailRequest request = new EmailRequest(
                    reservation.getContactEmail(),
                    "Falcon Airlines Reservation Confirmation - " + reservation.getNumber(),
                    html,
                    true,
                    List.of(logoInline),
                    List.of()
            );

            emailService.send(request);
            log.info("Reservation Confirmation email sent successfully to {}", reservation.getContactEmail());
        } catch (Exception e) {
            log.error("Failed to send reservation confirmation email for Reservation ID: {}", reservationId, e);
        }
    }

    private void checkPassengersAlreadyReservedFlight(List<PaymentPassengerDto> passengers, FlightEntity flight) {
        for (PaymentPassengerDto dto : passengers) {
            try {
                PassengerEntity passenger = passengerService.
                        getPassengerEntityByIdentificationNumber(dto.getPassenger().identificationNumber(), dto.getPassenger().nationalityIsoCode());

                if (!passengerReservationRepository.findAllByFlightAndPassengerAndStatusNot(flight, passenger, PassengerReservationStatus.CANCELED).isEmpty()) {
                    throw new PassengerAlreadyReservedFlightException(passenger.getIdentification(), flight.getId());
                }
            } catch (PassengerNotFoundException e) {
                continue;
            }
        }
    }

    @Transactional
    public ResponseReservationDto cancelPassengerReservationByIdentificationNumber(String reservationNumber, String contactEmail, String identificationNumber, String countryIsoCode) {
        ReservationEntity reservation = getReservationAvailableForGuestCancellation(reservationNumber, contactEmail);
        PassengerEntity passenger = passengerService.getPassengerEntityByIdentificationNumber(identificationNumber, countryIsoCode);
        return reservationMapper.toResponseDto(cancelPassengerReservation(reservation, passenger));
    }

    @Transactional
    public ResponseReservationDto cancelPassengerReservationByPassportNumber(String reservationNumber, String contactEmail, String passportNumber) {
        ReservationEntity reservation = getReservationAvailableForGuestCancellation(reservationNumber, contactEmail);
        PassengerEntity passenger = passengerService.getPassengerEntityByPassportNumber(passportNumber);
        return reservationMapper.toResponseDto(cancelPassengerReservation(reservation, passenger));
    }

    public ReservationEntity cancelPassengerReservation(ReservationEntity reservation, PassengerEntity passenger) {
        reservation.cancelPassenger(passenger);
        log.info("Passenger with id {} canceled in reservation {}", passenger.getId(), reservation.getNumber());
        return reservation;
    }

    @Transactional
    public ResponseReservationDto cancelReservation(String reservationNumber) {
        ReservationEntity reservationEntity = reservationQueryService.getReservationEntityByNumber(reservationNumber);
        reservationEntity.cancel();
        log.info("Reservation number {} has been canceled", reservationEntity.getNumber());
        return reservationMapper.toResponseDto(reservationEntity);
    }

    @Transactional
    public ResponseReservationDto cancelReservationByContactEmail(String reservationNumber, String contactEmail) {
        ReservationEntity reservation = getReservationAvailableForGuestCancellation(reservationNumber, contactEmail);
        reservation.cancel();
        log.info("Reservation number {} has been canceled by contact email verification", reservation.getNumber());
        return reservationMapper.toResponseDto(reservation);
    }

    @Transactional
    public int completeReservationsForFlight(FlightEntity flight) {
        List<ReservationEntity> reservations = reservationRepository.findAllByFlightAndStatus(flight, ReservationStatus.RESERVED);

        int count = 0;
        for (ReservationEntity reservation : reservations) {
            reservation.markAsCompleted();
            count++;
        }

        if (count > 0) {
            log.info("Completed {} reservation(s) whose flight departure has passed", count);
        }

        return count;
    }

    private ReservationEntity getReservationAvailableForGuestCancellation(String reservationNumber, String contactEmail) {
        ReservationEntity reservation = reservationAccessService.getReservationByNumberAndContactEmail(reservationNumber, contactEmail);
        Instant cutoff = reservation.getFlight().getDepartureDateTime().toInstant().minus(minimumHoursBeforeDeparture, ChronoUnit.HOURS);
        if (Instant.now().isAfter(cutoff)) {
            throw new ReservationCancellationTimeExpiredException(reservationNumber, minimumHoursBeforeDeparture);
        }
        return reservation;
    }
}
