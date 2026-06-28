package pl.kacper.reservation.hotelReservationSystem.dtos;

import java.util.List;

public record  PageResultDto <T>(
        int currentPage,
        int maxPage,
        long totalElements,
        int elementsOnPage,
        List<T> content
) {
}
