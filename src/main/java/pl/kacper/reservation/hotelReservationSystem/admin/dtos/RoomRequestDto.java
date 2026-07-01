package pl.kacper.reservation.hotelReservationSystem.admin.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomEntity;

import java.math.BigDecimal;

public record RoomRequestDto(
        @Min(1) long roomNumber,
        @Min(1) int roomCapacity,
        @Min(1) BigDecimal pricePerNight,
        @NotNull RoomEntity.StandardType standardType
) {
}
