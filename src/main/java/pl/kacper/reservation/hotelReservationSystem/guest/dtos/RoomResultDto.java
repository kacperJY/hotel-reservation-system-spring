package pl.kacper.reservation.hotelReservationSystem.guest.dtos;

import org.apache.juli.logging.Log;
import pl.kacper.reservation.hotelReservationSystem.catalog.Address;
import pl.kacper.reservation.hotelReservationSystem.catalog.FacilityEntity;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomEntity;

import java.math.BigDecimal;
import java.util.List;

public record RoomResultDto(
        Long roomId,
        Long facilityId,
        String facilityName,
        String facilityDescription,
        List<String> facilityAmenities,
        FacilityEntity.FacilityType facilityType,
        Address address,
        int roomCapacity,
        BigDecimal pricePerNight,
        BigDecimal fullPrice,
        String currency,
        long nights,
        RoomEntity.StandardType roomStandardType
) {
}
