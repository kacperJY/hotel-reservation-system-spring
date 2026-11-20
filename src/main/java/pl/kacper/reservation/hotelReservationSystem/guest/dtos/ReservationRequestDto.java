package pl.kacper.reservation.hotelReservationSystem.guest.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationRequestDto(
        @NotNull Long roomId,
        @NotNull Long facilityId,
        @NotNull @FutureOrPresent LocalDate startDate,
        @NotNull LocalDate endDate
) {

    @JsonIgnore
    @AssertTrue(message = "endDate must be after startDate")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) return true; // reported by @NotNull
        return endDate.isAfter(startDate);
    }
}
