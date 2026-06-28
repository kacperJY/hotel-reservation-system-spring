package pl.kacper.reservation.hotelReservationSystem.admin.dtos;


import jakarta.validation.constraints.NotBlank;
import pl.kacper.reservation.hotelReservationSystem.catalog.Address;
import pl.kacper.reservation.hotelReservationSystem.catalog.FacilityEntity;

import java.util.List;

public record FacilityRequestDto(
        @NotBlank String name,
        @NotBlank String description,
        List<String> amenities,
        FacilityEntity.FacilityType facilityType,
        Address address
) {
}
