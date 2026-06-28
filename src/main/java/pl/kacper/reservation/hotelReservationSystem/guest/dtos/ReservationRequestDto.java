package pl.kacper.reservation.hotelReservationSystem.guest.dtos;

import java.time.LocalDate;

public record ReservationRequestDto(
        Long roomId,
        Long facilityId,
        LocalDate startDate,
        LocalDate endDate
) {
}
