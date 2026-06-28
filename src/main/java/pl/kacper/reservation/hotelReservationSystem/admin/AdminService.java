package pl.kacper.reservation.hotelReservationSystem.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kacper.reservation.hotelReservationSystem.admin.dtos.FacilityRequestDto;
import pl.kacper.reservation.hotelReservationSystem.admin.dtos.FacilityResponseDto;
import pl.kacper.reservation.hotelReservationSystem.admin.dtos.RoomRequestDto;
import pl.kacper.reservation.hotelReservationSystem.admin.dtos.RoomResponseDto;
import pl.kacper.reservation.hotelReservationSystem.listeners.events.RoomEventDto;
import pl.kacper.reservation.hotelReservationSystem.repositories.FacilityRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.RoomRepository;
import pl.kacper.reservation.hotelReservationSystem.catalog.FacilityEntity;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomEntity;
import pl.kacper.reservation.hotelReservationSystem.exception.RecordNotExistsDbException;
import pl.kacper.reservation.hotelReservationSystem.user.Role;
import pl.kacper.reservation.hotelReservationSystem.user.UserEntity;
import pl.kacper.reservation.hotelReservationSystem.repositories.UserRepository;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;
    private final RoomRepository roomRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public AdminService(UserRepository userRepository, FacilityRepository facilityRepository, RoomRepository roomRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.userRepository = userRepository;
        this.facilityRepository = facilityRepository;
        this.roomRepository = roomRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public FacilityResponseDto createFacility(FacilityRequestDto facilityRequestDto) {

        FacilityEntity facilityEntity = new FacilityEntity(
                facilityRequestDto.name(),
                facilityRequestDto.description(),
                facilityRequestDto.amenities(),
                facilityRequestDto.facilityType(),
                facilityRequestDto.address()
        );

        facilityRepository.save(facilityEntity);

        return new FacilityResponseDto(facilityEntity.getFacilityId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public RoomResponseDto createRoom(Long facilityId, RoomRequestDto roomRequestDto) {

        FacilityEntity facilityReference = facilityRepository.findById(facilityId).orElseThrow(() -> new RecordNotExistsDbException("Cannot create room and assign to no exists facility"));

        RoomEntity roomEntity = new RoomEntity(
                roomRequestDto.roomNumber(),
                roomRequestDto.roomCapacity(),
                roomRequestDto.pricePerNight(),
                roomRequestDto.standardType(),
                facilityReference
        );

        roomRepository.save(roomEntity);

        applicationEventPublisher.publishEvent(new RoomEventDto(roomEntity.getRoomId()));

        return new RoomResponseDto(roomEntity.getRoomId(), facilityReference.getFacilityId());
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void addEmployeeToFacility(Long facilityId, Long employeeId) {

        FacilityEntity facilityEntity = facilityRepository.findById(facilityId).orElseThrow(() -> new RecordNotExistsDbException("Cannot add employee to no exists facility"));

        UserEntity userEntity = userRepository.findById(employeeId).orElseThrow(() -> new RecordNotExistsDbException("Cannot add employee that not exists to facility"));
        userEntity.setRole(Role.ROLE_MANAGER);

        facilityEntity.addEmployee(userEntity);
    }
}
