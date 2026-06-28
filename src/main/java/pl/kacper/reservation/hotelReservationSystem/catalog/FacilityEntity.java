package pl.kacper.reservation.hotelReservationSystem.catalog;

import jakarta.persistence.*;
import pl.kacper.reservation.hotelReservationSystem.user.UserEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "facilities")
public class FacilityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "facilityGen")
    @SequenceGenerator(name = "facilityGen", sequenceName = "facilitySeq", allocationSize = 50)
    @Column(name = "facility_id")
    private Long facilityId;

    private String name;

    @Column(length = 1024)
    private String description;

    private List<String> amenities;

    @Enumerated(EnumType.STRING)
    private FacilityType facilityType;

    @Embedded
    private Address address;

    @OneToMany(mappedBy = "facility", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE}, orphanRemoval = true)
    private List<RoomEntity> roomList = new ArrayList<>();

    @OneToMany(mappedBy = "facilityEntity",fetch = FetchType.LAZY)
    private List<UserEntity> employees = new ArrayList<>();

    public enum FacilityType {
        HOTEL, APARTMENT
    }

    public void addRoom(RoomEntity roomEntity) {
        roomEntity.setFacility(this);

        roomList.add(roomEntity);
    }

    public void addEmployee(UserEntity userEntity){
        userEntity.setFacilityEntity(this);
        employees.add(userEntity);
    }

    // CONSTRUCTOR

    public FacilityEntity() {
    }

    public FacilityEntity(String name, String description, List<String> amenities, FacilityType facilityType, Address address) {
        this.name = name;
        this.description = description;
        this.amenities = amenities;
        this.facilityType = facilityType;
        this.address = address;
    }

    // GETTER & SETTER


    public List<UserEntity> getEmployees() {
        return employees;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public List<RoomEntity> getRoomList() {
        return roomList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    public FacilityType getFacilityType() {
        return facilityType;
    }

    public void setFacilityType(FacilityType facilityType) {
        this.facilityType = facilityType;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
