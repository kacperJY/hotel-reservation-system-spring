package pl.kacper.reservation.hotelReservationSystem.guest.dtos;

import pl.kacper.reservation.hotelReservationSystem.catalog.Address;
import pl.kacper.reservation.hotelReservationSystem.catalog.ReservationEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReservationHistoryResponseDto(
        Long reservationId,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        long nights,
        ReservationEntity.Status reservationStatus,
        BigDecimal fullPrice,
        String hotelName,
        Address address,
        int roomCapacity,
        ReservationHistoryStatus reservationHistoryStatus
) {

    public enum ReservationHistoryStatus{
        FUTURE, PAST
    }
}
