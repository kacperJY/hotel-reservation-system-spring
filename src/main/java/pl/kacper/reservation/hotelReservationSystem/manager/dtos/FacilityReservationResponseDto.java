package pl.kacper.reservation.hotelReservationSystem.manager.dtos;

import pl.kacper.reservation.hotelReservationSystem.dtos.ReservationResponseDto;

import java.util.List;

public record FacilityReservationResponseDto(
        Long facilityId,
        List<ReservationResponseDto> reservationList
) {
}
