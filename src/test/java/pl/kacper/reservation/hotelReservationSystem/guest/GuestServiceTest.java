package pl.kacper.reservation.hotelReservationSystem.guest;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import pl.kacper.reservation.hotelReservationSystem.catalog.*;
import pl.kacper.reservation.hotelReservationSystem.dtos.CurrencyDto;
import pl.kacper.reservation.hotelReservationSystem.dtos.PageResultDto;
import pl.kacper.reservation.hotelReservationSystem.exception.NoElementsException;
import pl.kacper.reservation.hotelReservationSystem.exception.RecordNotExistsDbException;
import pl.kacper.reservation.hotelReservationSystem.exception.RoomAlreadyReservedException;
import pl.kacper.reservation.hotelReservationSystem.guest.dtos.ReservationHistoryResponseDto;
import pl.kacper.reservation.hotelReservationSystem.guest.dtos.ReservationRequestDto;
import pl.kacper.reservation.hotelReservationSystem.guest.dtos.RoomResultDto;
import pl.kacper.reservation.hotelReservationSystem.repositories.ReservationRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.RoomAvailabilityRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.RoomRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.UserRepository;
import pl.kacper.reservation.hotelReservationSystem.services.CurrencyService;
import pl.kacper.reservation.hotelReservationSystem.user.UserEntity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.StatusResultMatchersExtensionsKt.isEqualTo;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(MockitoExtension.class)
class GuestServiceTest {

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private RoomAvailabilityRepository roomAvailabilityRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private GuestService guestService;

    @Test
    @DisplayName("Should return 3600 PLN for 9 nights and Price 400 PLN per night")
    void should9NightAnd400PerNightReturn3600() {
        long nights = 9;
        BigDecimal pricePerNight = BigDecimal.valueOf(400);

        BigDecimal result = guestService.calculateFullPrice(nights, pricePerNight);

        Assertions.assertThat(result).isEqualTo(new BigDecimal(3600));
    }

    @Test
    @DisplayName("Should return 900.00 EUR (1 EUR = 4.5 PLN) for 9 nights and 450 PLN per night")
    void should9NightAnd450PerNightReturn900EUR(){
        long nights = 9;
        BigDecimal pricePerNight = new BigDecimal("450");
        String currencyCode = "EUR";

        CurrencyEntity currencyEntity = new CurrencyEntity(null,currencyCode,new BigDecimal("4.5"),null);

        Mockito.when(currencyService.getCurrencyRate(currencyCode)).thenReturn(currencyEntity);

        BigDecimal result = guestService.calculateFullPrice(nights, pricePerNight, currencyCode);

        Assertions.assertThat(result).isEqualByComparingTo("900.00");

    }

    // findRooms

    @Test
    @DisplayName("Should return empty list of Dtos when dates are invalid -> startDate >= endDate")
    void shouldReturnEmptyListForInvalidReservationDates() {
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().minusDays(5);

        List<RoomResultDto> rooms = guestService.findRooms(null, null, "PLN", startDate, endDate);

        Assertions.assertThat(rooms).isEmpty();
    }

    // createReservation
    @Test
    @DisplayName("Should create Reservation successfully")
    void shouldCreateReservationSuccessfully() {
        long roomId = 1L;
        long facilityId = 10L;
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(1);

        UserEntity userEntity = new UserEntity("email@", null, null);
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(roomId, facilityId, start, end);

        RoomEntity mockRoom = new RoomEntity(10, 2, BigDecimal.valueOf(100), RoomEntity.StandardType.NORMAL, null);
        ReflectionTestUtils.setField(mockRoom, "roomId", roomId);

        Mockito.when(roomRepository.findByRoomIdAndFacility_FacilityId(roomId, facilityId))
                .thenReturn(Optional.of(mockRoom));

        Mockito.when(userRepository.findByEmail("email@"))
                .thenReturn(Optional.of(userEntity));

        Mockito.when(roomAvailabilityRepository.findAvailabilityByDatesAndRoomId(roomId, start, end))
                .thenReturn(List.of(
                        new RoomAvailabilityEntity(null, null, 1),
                        new RoomAvailabilityEntity(null, null, 1) // (skróciłem dla czytelności)
                ));

        ArgumentCaptor<ReservationEntity> argumentCaptor = ArgumentCaptor.forClass(ReservationEntity.class);

        guestService.createReservation(reservationRequestDto, userEntity);

        Mockito.verify(reservationRepository).save(argumentCaptor.capture());

        ReservationEntity reservationEntity = argumentCaptor.getValue();
        Assertions.assertThat(reservationEntity.getCheckIn()).isEqualTo(start);
        Assertions.assertThat(reservationEntity.getCheckOut()).isEqualTo(end);
        Assertions.assertThat(reservationEntity.getRoomEntity().getRoomId()).isEqualTo(roomId);
    }

    @Test
    @DisplayName("Should throw RoomAlreadyReservedException when room is already reserved")
    void shouldThrowExceptionWhenRoomIsAlreadyReserved() {
        //given
        long roomId = 1L;
        long facilityId = 10L;
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(1);

        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(roomId, facilityId, start, end);

        UserEntity userEntity = new UserEntity("email@", null, null);

        RoomEntity mockRoom = new RoomEntity(10, 2, BigDecimal.valueOf(100), RoomEntity.StandardType.NORMAL, null);
        ReflectionTestUtils.setField(mockRoom, "roomId", roomId);

        // when
        Mockito.when(roomRepository.findByRoomIdAndFacility_FacilityId(ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong()))
                .thenReturn(Optional.of(mockRoom));

        Mockito.when(userRepository.findByEmail(ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(userEntity));

        Mockito.when(roomAvailabilityRepository.findAvailabilityByDatesAndRoomId(ArgumentMatchers.anyLong(), ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(Collections.emptyList()); // emptyList = no free days to reserve

        Assertions.assertThatThrownBy(() -> guestService.createReservation(reservationRequestDto, userEntity))
                .isInstanceOf(RoomAlreadyReservedException.class)
                .hasMessageContaining("Cannot reserve chosen room because this room is already reserved");
    }

    @Test
    @DisplayName("Should throw RecordNotExists when roomId or facilityId is invalid")
    void shouldThrowExceptionWhenRoomIdOrFacilityIdIsInvalid() {
        long roomId = 1L;
        long facilityId = 10L;
        LocalDate start = LocalDate.now().minusDays(1);
        LocalDate end = LocalDate.now().plusDays(1);
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(roomId, facilityId, start, end);

        Mockito.when(roomRepository.findByRoomIdAndFacility_FacilityId(ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> guestService.createReservation(reservationRequestDto, null))
                .isInstanceOf(RecordNotExistsDbException.class)
                .hasMessageContaining("Cannot reserve room that not exists");
    }

    // getUserReservationHistory

    @Test
    @DisplayName("Should throw IllegalArgumentException when page number is < 1")
    void shouldThrowExceptionWhenInvalidPageNumber() {
        int wrongPageNumber = 0;

        UserEntity userEntity = new UserEntity("email@", null, null);

        Mockito.when(userRepository.findByEmail(ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(userEntity));

        Assertions.assertThatThrownBy(() -> guestService.getUserReservationHistory(userEntity, wrongPageNumber))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid page number");
    }

    @Test
    @DisplayName("Should throw NoElementsException when there are not any reservation for passed facility")
    void shouldThrowExceptionWhenNoReservationForFacility() {
        long userId = 5L;

        UserEntity userEntity = new UserEntity("email@", null, null);
        ReflectionTestUtils.setField(userEntity, "userId", userId);

        Mockito.when(userRepository.findByEmail(ArgumentMatchers.anyString()))
                .thenReturn(Optional.of(userEntity));

        Mockito.when(reservationRepository.findByReservationOwner_UserId(ArgumentMatchers.anyLong(), ArgumentMatchers.any()))
                .thenReturn(Page.empty());


        Assertions.assertThatThrownBy(() -> guestService.getUserReservationHistory(userEntity, 1))
                .isInstanceOf(NoElementsException.class)
                .hasMessageContaining("There are no reservation for this user");
    }

    @Test
    @DisplayName("Should successfully return user reservation history page")
    void shouldReturnUserReservationHistorySuccessfully() {
        // given
        long userId = 5L;
        String email = "email@";
        UserEntity userEntity = new UserEntity(email, null, null);
        ReflectionTestUtils.setField(userEntity, "userId", userId);

        pl.kacper.reservation.hotelReservationSystem.catalog.FacilityEntity facility =
                new pl.kacper.reservation.hotelReservationSystem.catalog.FacilityEntity();
        ReflectionTestUtils.setField(facility, "name", "Grand Hotel");
        ReflectionTestUtils.setField(facility, "address",
                new Address("City", "Street", "1", "00-000"));

        RoomEntity room = new RoomEntity(10, 2, BigDecimal.valueOf(100), RoomEntity.StandardType.NORMAL, facility);

        ReservationEntity reservation = new ReservationEntity(
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                BigDecimal.valueOf(50),
                ReservationEntity.Status.PENDING,
                room,
                userEntity
        );
        ReflectionTestUtils.setField(reservation, "reservationId", 100L);

        Page<ReservationEntity> page = new org.springframework.data.domain.PageImpl<>(
                List.of(reservation), PageRequest.of(0, 10), 1
        );

        Mockito.when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(userEntity));

        Mockito.when(reservationRepository.findByReservationOwner_UserId(ArgumentMatchers.eq(userId), ArgumentMatchers.any()))
                .thenReturn(page);

        // when
        PageResultDto<ReservationHistoryResponseDto> result = guestService.getUserReservationHistory(userEntity, 1);

        // then
        Assertions.assertThat(result.totalElements()).isEqualTo(1);
        Assertions.assertThat(result.content()).hasSize(1);
        Assertions.assertThat(result.content().get(0).hotelName()).isEqualTo("Grand Hotel");
        Assertions.assertThat(result.content().get(0).reservationStatus()).isEqualTo(ReservationEntity.Status.PENDING);
    }

    // cancelReservation

    @Test
    @DisplayName("Should successfully cancel reservation and release room slots")
    void shouldCancelReservationSuccessfully() {
        // given
        long reservationId = 100L;
        String email = "email@";
        UserEntity user = new UserEntity(email, null, null);

        RoomEntity room = new RoomEntity();
        ReflectionTestUtils.setField(room, "roomId", 10L);

        ReservationEntity reservation = new ReservationEntity(
                LocalDate.now().plusDays(5), // Zapas > 2 dni
                LocalDate.now().plusDays(7),
                BigDecimal.valueOf(50),
                ReservationEntity.Status.PENDING,
                room,
                user
        );
        ReflectionTestUtils.setField(reservation, "reservationId", reservationId);

        RoomAvailabilityEntity takenDate = new RoomAvailabilityEntity(null, null, 0);

        Mockito.when(reservationRepository.findFullReservationByReservationId(reservationId))
                .thenReturn(Optional.of(reservation));

        Mockito.when(roomAvailabilityRepository.findTakenByDatesAndRoomId(10L, reservation.getCheckIn(), reservation.getCheckOut()))
                .thenReturn(List.of(takenDate));

        // when
        guestService.cancelReservation(reservationId, user);

        // then
        Assertions.assertThat(reservation.getStatus()).isEqualTo(ReservationEntity.Status.CANCELLED);
        Assertions.assertThat(takenDate.getFreeSlots()).isEqualTo(1); // Upewniamy się, że z powrotem jest 1
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when trying to cancel someone else's reservation")
    void shouldThrowExceptionWhenCancelingSomeoneElseReservation() {
        long reservationId = 100L;
        UserEntity currentUser = new UserEntity("hacker@", null, null);
        UserEntity ownerUser = new UserEntity("owner@", null, null);

        ReservationEntity reservation = new ReservationEntity(null, null, BigDecimal.ZERO, null, null, ownerUser);

        Mockito.when(reservationRepository.findFullReservationByReservationId(reservationId))
                .thenReturn(Optional.of(reservation));

        Assertions.assertThatThrownBy(() -> guestService.cancelReservation(reservationId, currentUser))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class)
                .hasMessageContaining("Cannot get access to someone else reservation");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when canceling less than 2 days before check-in")
    void shouldThrowExceptionWhenCancelingTooLate() {
        long reservationId = 100L;
        UserEntity user = new UserEntity("email@", null, null);

        ReservationEntity reservation = new ReservationEntity(
                LocalDate.now().plusDays(1), // Tylko 1 dzień zapasu!
                LocalDate.now().plusDays(3),
                BigDecimal.valueOf(50),
                ReservationEntity.Status.PENDING,
                new RoomEntity(),
                user
        );

        Mockito.when(reservationRepository.findFullReservationByReservationId(reservationId))
                .thenReturn(Optional.of(reservation));

        Assertions.assertThatThrownBy(() -> guestService.cancelReservation(reservationId, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("less than 2 days before the reservation date");
    }

    @Test
    @DisplayName("Should throw IllegalStateException when reservation status is not PENDING")
    void shouldThrowExceptionWhenStatusIsNotPending() {
        long reservationId = 100L;
        UserEntity user = new UserEntity("email@", null, null);

        ReservationEntity reservation = new ReservationEntity(
                LocalDate.now().plusDays(5),
                LocalDate.now().plusDays(7),
                BigDecimal.valueOf(50),
                ReservationEntity.Status.CONFIRMED,
                new RoomEntity(),
                user
        );

        Mockito.when(reservationRepository.findFullReservationByReservationId(reservationId))
                .thenReturn(Optional.of(reservation));

        Assertions.assertThatThrownBy(() -> guestService.cancelReservation(reservationId, user))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PENDING reservations can be cancelled");
    }
}
