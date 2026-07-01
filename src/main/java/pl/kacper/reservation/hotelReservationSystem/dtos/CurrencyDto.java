package pl.kacper.reservation.hotelReservationSystem.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CurrencyDto(
        @JsonProperty("currency") String currencyName,
        @JsonProperty("code") String currencyCode,
        @JsonProperty("mid") BigDecimal averageRate
        ) {
}
