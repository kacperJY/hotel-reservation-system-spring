package pl.kacper.reservation.hotelReservationSystem.manager;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kacper.reservation.hotelReservationSystem.dtos.PageResultDto;
import pl.kacper.reservation.hotelReservationSystem.dtos.ReservationResponseDto;
import pl.kacper.reservation.hotelReservationSystem.manager.dtos.ReservationStatusRequestDto;

@RestController
@RequestMapping("/api/managers")
public class ManagerController {

    private final ManagerService managerService;

    public ManagerController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @GetMapping("facilities/{facilityId}/reservations")
    public ResponseEntity<PageResultDto> getReservationsByFacility(
            @PathVariable("facilityId") Long facilityId,
            @RequestParam(value = "page", defaultValue = "1") int pageNumber
            ){

        PageResultDto<ReservationResponseDto> allReservationsByFacility = managerService.getAllReservationsByFacility(facilityId, pageNumber);

        return ResponseEntity.ok(allReservationsByFacility);
    }

    @PatchMapping("facilities/{facilityId}/reservations/{reservationID}")
    public ResponseEntity<Void> updateReservationStatus(
            @PathVariable("facilityId") Long facilityId,
            @PathVariable("reservationID") Long reservationId,
            @RequestBody ReservationStatusRequestDto reservationStatusRequestDto
            ){

        managerService.updateReservationStatus(facilityId,reservationId,reservationStatusRequestDto);

        return ResponseEntity.ok().build();
    }
}
