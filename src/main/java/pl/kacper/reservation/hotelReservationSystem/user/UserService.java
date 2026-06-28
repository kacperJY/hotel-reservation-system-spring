package pl.kacper.reservation.hotelReservationSystem.user;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.kacper.reservation.hotelReservationSystem.repositories.UserRepository;
import pl.kacper.reservation.hotelReservationSystem.security.JWTService;
import pl.kacper.reservation.hotelReservationSystem.user.dto.UserLoginResponseDto;
import pl.kacper.reservation.hotelReservationSystem.user.dto.UserRequestDto;
import pl.kacper.reservation.hotelReservationSystem.user.dto.UserResponseDto;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;

    public UserService(PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, UserRepository userRepository, JWTService jwtService, UserDetailsService userDetailsService) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    public UserLoginResponseDto login(UserRequestDto userRequestDto){

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                userRequestDto.email(),
                userRequestDto.password()
        ));
        UserDetails userDetails = userDetailsService.loadUserByUsername(userRequestDto.email());
        String token = jwtService.generateToken(userDetails);
        return new UserLoginResponseDto(token);
    }

    public UserResponseDto register(UserRequestDto userRequestDto){
        String rawPassword = userRequestDto.password();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        UserEntity userEntity = new UserEntity(
                userRequestDto.email(),
                encodedPassword,
                userRequestDto.phoneNumber(),
                Role.ROLE_GUEST);


        userRepository.save(userEntity);

        return new UserResponseDto(userEntity.getUserId());
    }
}
