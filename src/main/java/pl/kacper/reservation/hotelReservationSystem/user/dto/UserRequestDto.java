package pl.kacper.reservation.hotelReservationSystem.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDto(
        @Email String email,
        @Size(min = 8, max = 24) String password,

        @Pattern(regexp = "^\\d{9}$", message = "Number must contains only 9 numbers")
        String phoneNumber
) {
}
