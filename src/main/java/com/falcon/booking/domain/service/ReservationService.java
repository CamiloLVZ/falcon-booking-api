package com.falcon.booking.domain.service;

import com.falcon.booking.domain.mapper.FlightMapper;
import com.falcon.booking.domain.mapper.PassengerReservationMapper;
import com.falcon.booking.persistence.entity.FlightEntity;
import com.falcon.booking.persistence.entity.PassengerEntity;
import com.falcon.booking.persistence.entity.PassengerReservationEntity;
import com.falcon.booking.persistence.entity.ReservationEntity;
import com.falcon.booking.persistence.repository.PassengerReservationRepository;
import com.falcon.booking.persistence.repository.ReservationRepository;
import com.falcon.booking.web.dto.reservation.AddPassengerReservationDto;
import com.falcon.booking.web.dto.reservation.AddReservationDto;
import com.falcon.booking.web.dto.reservation.ResponsePassengerReservationDto;
import com.falcon.booking.web.dto.reservation.ResponseReservationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PassengerReservationRepository passengerReservationRepository;
    private final FlightService flightService;
    private final PassengerService passengerService;
    private final FlightMapper flightMapper;
    private final PassengerReservationMapper passengerReservationMapper;

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, PassengerReservationRepository passengerReservationRepository, FlightService flightService, PassengerService passengerService, FlightMapper flightMapper, PassengerReservationMapper passengerReservationMapper) {
        this.reservationRepository = reservationRepository;
        this.passengerReservationRepository = passengerReservationRepository;
        this.flightService = flightService;
        this.passengerService = passengerService;
        this.flightMapper = flightMapper;
        this.passengerReservationMapper = passengerReservationMapper;
    }

    private String generateReservationNumber(){
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        int reservationNumberLength = 6;
        StringBuilder reservationNumber;
        boolean alreadyExists;
        do{
            reservationNumber = new StringBuilder();
            for(int i = 0; i < reservationNumberLength; i++) {
                int index = ThreadLocalRandom.current().nextInt(0, alphabet.length());
                reservationNumber.append(alphabet.charAt(index));
            }
            alreadyExists = reservationRepository.existsByNumber(reservationNumber.toString());
        }while(alreadyExists);
        return reservationNumber.toString();
    }


    @Transactional
    public ResponseReservationDto addReservation(AddReservationDto addReservationDto) {
        FlightEntity flightEntity = flightService.getFlightEntity(addReservationDto.idFlight());
        String reservationNumber = generateReservationNumber();
        ReservationEntity reservation = reservationRepository.save(
                new ReservationEntity(reservationNumber, flightEntity, addReservationDto.contactEmail(), Instant.now()));

        List<PassengerReservationEntity> passengerReservations = createPassengerReservationEntities(addReservationDto.passengers(), reservation);

        List<ResponsePassengerReservationDto> responsePassengerReservationDtos = passengerReservationMapper.toResponseDto( passengerReservationRepository.saveAll(passengerReservations));

       return new ResponseReservationDto(reservation.getNumber(), reservation.getContactEmail(),
               reservation.getDatetimeReservation(), flightMapper.toDto(flightEntity), responsePassengerReservationDtos);
    }

    private List<PassengerReservationEntity> createPassengerReservationEntities(List<AddPassengerReservationDto> addPassengersReservationsDto, ReservationEntity reservation) {
        List<PassengerReservationEntity> passengerReservations = new ArrayList<>();
        for (AddPassengerReservationDto addPassengerReservationDto : addPassengersReservationsDto) {
            PassengerEntity passenger = passengerService.createOrGetPassenger(addPassengerReservationDto.passenger());
            PassengerReservationEntity passengerReservation =
                    new PassengerReservationEntity(passenger, reservation, addPassengerReservationDto.seatNumber());
            passengerReservations.add(passengerReservation);
        }
        return passengerReservations;
    }


}
