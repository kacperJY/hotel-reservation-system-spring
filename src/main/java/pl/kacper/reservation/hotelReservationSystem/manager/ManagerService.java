package pl.kacper.reservation.hotelReservationSystem.manager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kacper.reservation.hotelReservationSystem.catalog.ReservationEntity;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomAvailabilityEntity;
import pl.kacper.reservation.hotelReservationSystem.dtos.PageResultDto;
import pl.kacper.reservation.hotelReservationSystem.dtos.ReservationResponseDto;
import pl.kacper.reservation.hotelReservationSystem.exception.NoElementsException;
import pl.kacper.reservation.hotelReservationSystem.manager.dtos.ReservationStatusRequestDto;
import pl.kacper.reservation.hotelReservationSystem.repositories.ReservationRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.RoomAvailabilityRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ManagerService {

    private final static int PAGE_SIZE = 20;

    private final ReservationRepository reservationRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;

    public ManagerService(ReservationRepository reservationRepository, RoomAvailabilityRepository roomAvailabilityRepository) {
        this.reservationRepository = reservationRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
    }

    @PreAuthorize("@managerSecurity.checkFacilityAccess(authentication.principal,#facilityId)")
    @Transactional(readOnly = true)
    public PageResultDto<ReservationResponseDto> getAllReservationsByFacility(Long facilityId, int originalPageNumber){
        int pageNumber = originalPageNumber - 1;
        if (originalPageNumber < 1) throw new IllegalArgumentException("Invalid page number. Minimal page number is 1");

        PageRequest reservationPageable = PageRequest.of(pageNumber,PAGE_SIZE);

        Page<ReservationEntity> reservationByFacility = reservationRepository.findReservationByFacility(facilityId, reservationPageable);

        long totalElements = reservationByFacility.getTotalElements();
        int elementsOnPage = reservationByFacility.getNumberOfElements();
        int maxPages = reservationByFacility.getTotalPages();

        if (totalElements == 0)
            throw new NoElementsException("There are no reservation for this facility");
        else if (elementsOnPage == 0)
            throw new IllegalArgumentException("Invalid page number %d/%d".formatted(originalPageNumber, maxPages));

        List<ReservationResponseDto> reservationResponseDtoList = new ArrayList<>(PAGE_SIZE);

        for (ReservationEntity reservationEntity : reservationByFacility.getContent()) {
            reservationResponseDtoList.add(
                    new ReservationResponseDto(
                            reservationEntity.getReservationId(),
                            reservationEntity.getStatus(),
                            reservationEntity.getCheckIn(),
                            reservationEntity.getCheckOut(),
                            reservationEntity.getFullPrice()
                    )
            );
        }

        return new PageResultDto<>(originalPageNumber,maxPages,totalElements,elementsOnPage,reservationResponseDtoList);
    }

    @PreAuthorize("@managerSecurity.checkFacilityAccess(authentication.principal,#facilityId)")
    @Transactional
    public void updateReservationStatus(Long facilityId, Long reservationId, ReservationStatusRequestDto reservationStatusRequestDto){
        ReservationEntity reservationEntity = reservationRepository.findByReservationIdAndRoomEntity_Facility_FacilityId(reservationId, facilityId)
                .orElseThrow(() -> new IllegalStateException("This reservations not belongs to picked facility"));

        ReservationEntity.Status nextStatus = reservationStatusRequestDto.reservationStatus();
        ReservationEntity.Status currentStatus = reservationEntity.getStatus();

        boolean isValid = validStatusFlow(currentStatus, nextStatus);

        if (!isValid) throw new IllegalStateException("Cannot change status=%s to %s".formatted(currentStatus,nextStatus));

        if(nextStatus == ReservationEntity.Status.CANCELLED){
            long roomId = reservationEntity.getRoomEntity().getRoomId();
            LocalDate checkIn = reservationEntity.getCheckIn();
            LocalDate checkOut = reservationEntity.getCheckOut();

            List<RoomAvailabilityEntity> takenByDatesAndRoomId = roomAvailabilityRepository.findTakenByDatesAndRoomId(roomId, checkIn, checkOut);
            takenByDatesAndRoomId.forEach(roomAvailabilityEntity -> roomAvailabilityEntity.setFreeSlots(1));
        }

        reservationEntity.setStatus(nextStatus);
    }

     boolean validStatusFlow(ReservationEntity.Status currentStatus, ReservationEntity.Status nextStatus){
        return switch (currentStatus) {
            case PENDING -> nextStatus == ReservationEntity.Status.CONFIRMED || nextStatus == ReservationEntity.Status.CANCELLED;
            case CONFIRMED -> nextStatus == ReservationEntity.Status.IN_PROGRESS || nextStatus == ReservationEntity.Status.CANCELLED;
            case IN_PROGRESS -> nextStatus == ReservationEntity.Status.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
