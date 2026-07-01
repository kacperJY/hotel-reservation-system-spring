package pl.kacper.reservation.hotelReservationSystem.exception;

public class ClientApiRequestException extends RuntimeException{

    public ClientApiRequestException(String message) {
        super(message);
    }

    public ClientApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
