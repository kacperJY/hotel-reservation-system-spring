package pl.kacper.reservation.hotelReservationSystem.exception;

public class FetchNoResultException extends RuntimeException {

    public FetchNoResultException(String message) {
        super(message);
    }
}
