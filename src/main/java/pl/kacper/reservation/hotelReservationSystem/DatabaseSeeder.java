package pl.kacper.reservation.hotelReservationSystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.kacper.reservation.hotelReservationSystem.admin.AdminService;
import pl.kacper.reservation.hotelReservationSystem.admin.dtos.RoomRequestDto;
import pl.kacper.reservation.hotelReservationSystem.catalog.Address;
import pl.kacper.reservation.hotelReservationSystem.catalog.FacilityEntity;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomEntity;
import pl.kacper.reservation.hotelReservationSystem.repositories.FacilityRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.UserRepository;
import pl.kacper.reservation.hotelReservationSystem.user.Role;
import pl.kacper.reservation.hotelReservationSystem.user.UserEntity;
import pl.kacper.reservation.hotelReservationSystem.user.UserService;
import pl.kacper.reservation.hotelReservationSystem.user.dto.UserRequestDto;

import java.util.List;

@Component
@Profile("dev")
public class DatabaseSeeder implements CommandLineRunner {

    private Environment environment;

    private final UserService userService;
    private final AdminService adminService;

    private final UserRepository userRepository;
    private final FacilityRepository facilityRepository;

    @Autowired
    public DatabaseSeeder(UserService userService, AdminService adminService, UserRepository userRepository, FacilityRepository facilityRepository, Environment environment) {
        this.userService = userService;
        this.adminService = adminService;
        this.userRepository = userRepository;
        this.facilityRepository = facilityRepository;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        String[] activeProfiles = this.environment.getActiveProfiles();
        System.out.println(" ACTIVE PROFILE ######################################");
        List.of(activeProfiles).forEach(System.out::println);

        UserRequestDto adminDto = new UserRequestDto("admin@hotel.com", "admin1234", "111222333");
        UserRequestDto managerGWHDto = new UserRequestDto("manager@gwh.com", "manager123", "444555666");
        UserRequestDto managerCOTADto = new UserRequestDto("manager@cota.com", "manager123", "444555666");
        UserRequestDto guestDto = new UserRequestDto("guest@hotel.com", "guest1234", "777888999");

        // Service - hashing password
        userService.register(adminDto);
        userService.register(managerGWHDto);
        userService.register(managerCOTADto);
        userService.register(guestDto);

        UserEntity adminEntity = userRepository.findByEmail(adminDto.email()).orElseThrow();
        UserEntity managerGWHEntity = userRepository.findByEmail(managerGWHDto.email()).orElseThrow();
        UserEntity managerCOTAEntity = userRepository.findByEmail(managerCOTADto.email()).orElseThrow();

        FacilityEntity hotel1 = new FacilityEntity(
                "Grand Warsaw Hotel",
                "Ekskluzywny 5-gwiazdkowy hotel w samym centrum Warszawy.",
                List.of("WiFi", "Basen", "SPA", "Restauracja", "Klimatyzacja"),
                FacilityEntity.FacilityType.HOTEL,
                new Address("Polska", "Warszawa", "Krakowskie Przedmieście 1", "00-001")
        );

        FacilityEntity apartment1 = new FacilityEntity(
                "Cracow Old Town Apartment",
                "Nowoczesny apartament zaledwie 50 metrów od Rynku Głównego.",
                List.of("WiFi", "Aneks kuchenny", "Netflix", "Pralka"),
                FacilityEntity.FacilityType.APARTMENT,
                new Address("Polska", "Kraków", "Floriańska 15", "31-021")
        );

        FacilityEntity hotel2 = new FacilityEntity(
                "Baltic Breeze Resort",
                "Rodzinny ośrodek wypoczynkowy z prywatnym zejściem na plażę.",
                List.of("WiFi", "Plaża", "Siłownia", "Plac zabaw", "Parking"),
                FacilityEntity.FacilityType.HOTEL,
                new Address("Polska", "Sopot", "Bohaterów Monte Cassino 50", "81-704")
        );

        FacilityEntity hotel1Entity = facilityRepository.save(hotel1);
        FacilityEntity hotel2Entity = facilityRepository.save(hotel2);
        FacilityEntity apartmentEntity = facilityRepository.save(apartment1);

        hotel1Entity.addEmployee(managerGWHEntity);
        hotel2Entity.addEmployee(managerCOTAEntity);

        // TEMPORARY AUTHENTICATION to skip PRE-AUTHORIZE SECURITY in AdminService
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                "adminTest",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        try {
            adminService.createRoom(hotel1Entity.getFacilityId(), new RoomRequestDto(101, 2, 350.0, RoomEntity.StandardType.NORMAL));
            adminService.createRoom(hotel2Entity.getFacilityId(), new RoomRequestDto(102, 2, 350.0, RoomEntity.StandardType.NORMAL));
            adminService.createRoom(apartmentEntity.getFacilityId(), new RoomRequestDto(201, 4, 600.0, RoomEntity.StandardType.PREMIUM));
        } finally {
            SecurityContextHolder.clearContext();
        }

        // UPDATE ROLE
        userRepository.updateRole(adminEntity.getUserId(), Role.ROLE_ADMIN);
        userRepository.updateRole(managerGWHEntity.getUserId(), Role.ROLE_MANAGER);
        userRepository.updateRole(managerCOTAEntity.getUserId(), Role.ROLE_MANAGER);
    }
}
