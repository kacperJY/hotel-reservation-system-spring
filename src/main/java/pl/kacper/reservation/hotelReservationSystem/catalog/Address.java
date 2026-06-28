package pl.kacper.reservation.hotelReservationSystem.catalog;

import jakarta.persistence.Embeddable;

@Embeddable
public record Address(

        String country,
        String city,
        String street,
        String postalCode
) {
}
