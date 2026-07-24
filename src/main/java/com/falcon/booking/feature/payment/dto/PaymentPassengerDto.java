package com.falcon.booking.feature.payment.dto;

import com.falcon.booking.common.enums.SeatClass;
import com.falcon.booking.feature.passenger.dto.AddPassengerDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentPassengerDto {
    @NotNull(message = "Passenger details can not be null")
    @Valid
    private AddPassengerDto passenger;

    @NotNull(message = "Seat class can not be null")
    private SeatClass seatClass;

    private BigDecimal unitPrice;

    public PaymentPassengerDto() {}

    public PaymentPassengerDto(AddPassengerDto passenger, SeatClass seatClass) {
        this.passenger = passenger;
        this.seatClass = seatClass;
    }

    public AddPassengerDto getPassenger() { return passenger; }
    public void setPassenger(AddPassengerDto passenger) { this.passenger = passenger; }

    public SeatClass getSeatClass() { return seatClass; }
    public void setSeatClass(SeatClass seatClass) { this.seatClass = seatClass; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
