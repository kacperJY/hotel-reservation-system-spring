package pl.kacper.reservation.hotelReservationSystem.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record CurrenciesTableDto(
        @JsonProperty("table") String table,
        @JsonProperty("no") String tableNumber,
        @JsonProperty("effectiveDate") LocalDate publicationData,
        @JsonProperty("rates") List<CurrencyDto> currencyDtoList
        ) {
}
