package pl.kacper.reservation.hotelReservationSystem.guest;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import pl.kacper.reservation.hotelReservationSystem.dtos.PageResultDto;
import pl.kacper.reservation.hotelReservationSystem.guest.dtos.ReservationHistoryResponseDto;
import pl.kacper.reservation.hotelReservationSystem.guest.dtos.ReservationRequestDto;
import pl.kacper.reservation.hotelReservationSystem.dtos.ReservationResponseDto;
import pl.kacper.reservation.hotelReservationSystem.guest.dtos.RoomResultDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomResultDto>> findRooms(
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = "guestNumber", required = false) Integer guestNumber,
            @RequestParam(name = "curr", required = false, defaultValue = "PLN") String currency,
            @RequestParam(name = "startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
            ){

        List<RoomResultDto> rooms = guestService.findRooms(city, guestNumber,currency, startDate, endDate);
        return ResponseEntity.ok(rooms);
    }

    @PostMapping("/rooms/reservations")
    public ResponseEntity<ReservationResponseDto> createReservation(@RequestBody @Valid ReservationRequestDto requestDto, @AuthenticationPrincipal UserDetails userDetails){

        ReservationResponseDto reservation = guestService.createReservation(requestDto, userDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @GetMapping("/rooms/reservations")
    public ResponseEntity<PageResultDto<ReservationHistoryResponseDto>> getUserReservations(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(name = "page", defaultValue = "1") int pageNumber){

        PageResultDto<ReservationHistoryResponseDto> userReservationHistory = guestService.getUserReservationHistory(userDetails, pageNumber);
        return ResponseEntity.ok(userReservationHistory);
    }

    @PatchMapping("/rooms/reservations/{reservationId}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable(name = "reservationId") Long reservationId, @AuthenticationPrincipal UserDetails userDetails){

        guestService.cancelReservation(reservationId,userDetails);

        return ResponseEntity.ok().build();
    }
}
