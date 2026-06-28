package pl.kacper.reservation.hotelReservationSystem.catalog;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "room_availability")
public class RoomAvailabilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roomAvailabilityGen")
    @SequenceGenerator(name = "roomAvailabilityGen", sequenceName = "roomAvailabilitySeq", allocationSize = 50)
    @Column(name = "room_availability_id")
    private Long roomAvailabilityId;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private RoomEntity roomEntity;

    private LocalDate date;

    private int freeSlots;

    // CONSTRUCTOR

    public RoomAvailabilityEntity(){}

    public RoomAvailabilityEntity(RoomEntity roomEntity, LocalDate date, int freeSlots) {
        this.roomEntity = roomEntity;
        this.date = date;
        this.freeSlots = freeSlots;
    }

    public Long getRoomAvailabilityId() {
        return roomAvailabilityId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getFreeSlots() {
        return freeSlots;
    }

    public void setFreeSlots(int freeSlots) {
        this.freeSlots = freeSlots;
    }

    public RoomEntity getRoomEntity() {
        return roomEntity;
    }

    public void setRoomEntity(RoomEntity roomEntity) {
        this.roomEntity = roomEntity;
    }
}
