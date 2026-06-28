package pl.kacper.reservation.hotelReservationSystem.guest.dtos;

import pl.kacper.reservation.hotelReservationSystem.catalog.Address;
import pl.kacper.reservation.hotelReservationSystem.catalog.ReservationEntity;

import java.time.LocalDate;

public record ReservationHistoryResponseDto(
        Long reservationId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        long nights,
        ReservationEntity.Status reservationStatus,
        double fullPrice,
        String hotelName,
        Address address,
        int roomCapacity,
        ReservationHistoryStatus reservationHistoryStatus
) {

    public enum ReservationHistoryStatus{
        FUTURE, PAST
    }
}
