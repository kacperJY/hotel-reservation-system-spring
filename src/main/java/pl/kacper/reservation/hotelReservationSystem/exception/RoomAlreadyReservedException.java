package pl.kacper.reservation.hotelReservationSystem.exception;

public class RoomAlreadyReservedException extends RuntimeException{

    public RoomAlreadyReservedException(String message) {
        super(message);
    }
}
