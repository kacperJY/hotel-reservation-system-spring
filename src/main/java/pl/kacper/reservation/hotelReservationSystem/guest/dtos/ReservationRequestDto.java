package pl.kacper.reservation.hotelReservationSystem.guest.dtos;

import org.jspecify.annotations.NonNull;

import java.time.LocalDate;

public record ReservationRequestDto(
        @NonNull Long roomId,
        @NonNull Long facilityId,
        @NonNull LocalDate startDate,
        @NonNull LocalDate endDate
) {
}
