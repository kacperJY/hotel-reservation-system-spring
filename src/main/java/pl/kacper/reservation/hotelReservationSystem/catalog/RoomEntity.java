package pl.kacper.reservation.hotelReservationSystem.catalog;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rooms")
public class RoomEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "roomGen")
    @SequenceGenerator(name = "roomGen", sequenceName = "roomSeq", allocationSize = 50)
    @Column(name = "room_id")
    private Long roomId;

    private long roomNumber;

    private int roomCapacity;

    private double pricePerNight;

    @Enumerated(EnumType.STRING)
    private StandardType standardType;

    public enum StandardType {
        NORMAL, PREMIUM
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private FacilityEntity facility;

    @OneToMany(mappedBy = "roomEntity")
    private List<ReservationEntity> reservationList = new ArrayList<>();


    public void addReservation(ReservationEntity reservation) {
        reservationList.add(reservation);
        reservation.setRoomEntity(this);
    }

    // CONSTRUCTOR
    public RoomEntity() {
    }

    public RoomEntity(long roomNumber, int roomCapacity, double pricePerNight, StandardType standardType, FacilityEntity facility) {
        this.roomNumber = roomNumber;
        this.roomCapacity = roomCapacity;
        this.pricePerNight = pricePerNight;
        this.standardType = standardType;
        this.facility = facility;
    }

    // GETTER & SETTER

    public List<ReservationEntity> getReservationList() {
        return reservationList;
    }

    public Long getRoomId() {
        return roomId;
    }

    public FacilityEntity getFacility() {
        return facility;
    }

    public void setFacility(FacilityEntity facilityEntity) {
        this.facility = facilityEntity;
    }

    public long getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(long roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getRoomCapacity() {
        return roomCapacity;
    }

    public void setRoomCapacity(int roomCapacity) {
        this.roomCapacity = roomCapacity;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricerPerNight) {
        this.pricePerNight = pricerPerNight;
    }

    public StandardType getStandardType() {
        return standardType;
    }

    public void setStandardType(StandardType standardType) {
        this.standardType = standardType;
    }
}
