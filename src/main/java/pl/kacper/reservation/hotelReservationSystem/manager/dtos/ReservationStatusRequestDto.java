package pl.kacper.reservation.hotelReservationSystem.manager.dtos;

import pl.kacper.reservation.hotelReservationSystem.catalog.ReservationEntity;

public record ReservationStatusRequestDto(
        ReservationEntity.Status reservationStatus
) {
}
