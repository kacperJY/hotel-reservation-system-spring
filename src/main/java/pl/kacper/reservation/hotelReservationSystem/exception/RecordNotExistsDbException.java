package pl.kacper.reservation.hotelReservationSystem.exception;

public class RecordNotExistsDbException extends RuntimeException{

    public RecordNotExistsDbException(String message) {
        super(message);
    }
}
