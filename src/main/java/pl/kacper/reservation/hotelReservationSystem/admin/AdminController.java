package pl.kacper.reservation.hotelReservationSystem.admin;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.kacper.reservation.hotelReservationSystem.admin.dtos.FacilityRequestDto;
import pl.kacper.reservation.hotelReservationSystem.admin.dtos.FacilityResponseDto;
import pl.kacper.reservation.hotelReservationSystem.admin.dtos.RoomRequestDto;
import pl.kacper.reservation.hotelReservationSystem.admin.dtos.RoomResponseDto;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/facilities")
    public ResponseEntity<FacilityResponseDto> createFacility(@RequestBody @Valid FacilityRequestDto facilityRequestDto) {
        FacilityResponseDto responseDto = adminService.createFacility(facilityRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/facilities/{facilityId}/rooms")
    public ResponseEntity<RoomResponseDto> createRoom(@PathVariable(name = "facilityId") Long facilityId, @RequestBody @Valid RoomRequestDto roomRequestDto) {
        RoomResponseDto roomResponseDto = adminService.createRoom(facilityId, roomRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(roomResponseDto);
    }

    @PatchMapping("/facilities/{facilityId}/employees/{employeeId}")
    public ResponseEntity<Void> addEmployeeToFacility(@PathVariable(name = "facilityId") Long facilityId, @PathVariable(name = "employeeId") Long employeeId){
        adminService.addEmployeeToFacility(facilityId,employeeId);

        return ResponseEntity.ok().build();
    }
}
