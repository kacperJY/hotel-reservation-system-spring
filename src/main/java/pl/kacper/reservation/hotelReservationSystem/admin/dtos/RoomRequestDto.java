package pl.kacper.reservation.hotelReservationSystem.admin.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomEntity;

public record RoomRequestDto(
        @Min(1) long roomNumber,
        @Min(1) int roomCapacity,
        @Min(1) double pricePerNight,
        @NotNull RoomEntity.StandardType standardType
) {
}
