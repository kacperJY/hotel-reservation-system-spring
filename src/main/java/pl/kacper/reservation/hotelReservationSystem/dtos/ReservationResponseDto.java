package pl.kacper.reservation.hotelReservationSystem.dtos;

import pl.kacper.reservation.hotelReservationSystem.catalog.ReservationEntity;

import java.time.LocalDate;

public record ReservationResponseDto(
        Long reservationId,
        ReservationEntity.Status reservationStatus,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        double fullPrice
) {
}
