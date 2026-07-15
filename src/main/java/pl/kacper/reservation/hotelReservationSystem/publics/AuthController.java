package pl.kacper.reservation.hotelReservationSystem.publics;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.kacper.reservation.hotelReservationSystem.user.UserService;
import pl.kacper.reservation.hotelReservationSystem.user.dto.UserLoginResponseDto;
import pl.kacper.reservation.hotelReservationSystem.user.dto.UserRequestDto;
import pl.kacper.reservation.hotelReservationSystem.user.dto.UserResponseDto;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public ResponseEntity<UserLoginResponseDto> login(@RequestBody UserRequestDto userRequestDto, HttpServletResponse httpServletResponse){
        UserLoginResponseDto responseDto = userService.login(userRequestDto);
        String token = responseDto.token();

        httpServletResponse.setHeader("Authorization", "Bearer " + token);

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody @Valid UserRequestDto userRequestDto){
        UserResponseDto responseDto = userService.register(userRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
