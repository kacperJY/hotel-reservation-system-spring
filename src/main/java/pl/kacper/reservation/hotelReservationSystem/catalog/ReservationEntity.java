package pl.kacper.reservation.hotelReservationSystem.catalog;

import jakarta.persistence.*;
import pl.kacper.reservation.hotelReservationSystem.user.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class ReservationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reservationGen")
    @SequenceGenerator(name = "reservationGen", sequenceName = "reservationSeq", allocationSize = 50)
    @Column(name = "reservation_id")
    private Long reservationId;

    private LocalDate checkIn;

    private LocalDate checkOut;

    private LocalDate createdAt;

    private double fullPrice;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private RoomEntity roomEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity reservationOwner;

    // CONSTRUCTOR

    public ReservationEntity() {
    }

    public ReservationEntity(LocalDate checkIn, LocalDate checkOut, double fullPrice, Status status, RoomEntity roomEntity, UserEntity userEntity) {
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.fullPrice = fullPrice;
        this.status = status;
        this.roomEntity = roomEntity;
        this.reservationOwner = userEntity;
        this.createdAt = LocalDate.now(); // DEFAULT
    }

    // GETTER & SETTER


    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public RoomEntity getRoomEntity() {
        return roomEntity;
    }

    public void setRoomEntity(RoomEntity roomEntity) {
        this.roomEntity = roomEntity;
    }

    public UserEntity getReservationOwner() {
        return reservationOwner;
    }

    public void setReservationOwner(UserEntity reservationOwner) {
        this.reservationOwner = reservationOwner;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public double getFullPrice() {
        return fullPrice;
    }

    public void setFullPrice(double fullPrice) {
        this.fullPrice = fullPrice;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public enum Status {
        PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
