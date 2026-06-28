package pl.kacper.reservation.hotelReservationSystem.admin;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import pl.kacper.reservation.hotelReservationSystem.admin.dtos.FacilityRequestDto;
import pl.kacper.reservation.hotelReservationSystem.admin.dtos.FacilityResponseDto;
import pl.kacper.reservation.hotelReservationSystem.admin.dtos.RoomRequestDto;
import pl.kacper.reservation.hotelReservationSystem.admin.dtos.RoomResponseDto;
import pl.kacper.reservation.hotelReservationSystem.catalog.Address;
import pl.kacper.reservation.hotelReservationSystem.catalog.FacilityEntity;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomEntity;
import pl.kacper.reservation.hotelReservationSystem.exception.RecordNotExistsDbException;
import pl.kacper.reservation.hotelReservationSystem.listeners.events.RoomEventDto;
import pl.kacper.reservation.hotelReservationSystem.repositories.FacilityRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.RoomRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.UserRepository;
import pl.kacper.reservation.hotelReservationSystem.user.Role;
import pl.kacper.reservation.hotelReservationSystem.user.UserEntity;

import java.util.List;
import java.util.Optional;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FacilityRepository facilityRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private AdminService adminService;

    // ==========================================
    // createFacility
    // ==========================================

    @Test
    @DisplayName("Should successfully create a new facility")
    void shouldCreateFacilitySuccessfully() {
        // given
        Address address = new Address("City", "Street", "1", "00-000");
        FacilityRequestDto requestDto = new FacilityRequestDto("Hotel Spa", "Desc", List.of("pool"), FacilityEntity.FacilityType.HOTEL, address);

        ArgumentCaptor<FacilityEntity> captor = ArgumentCaptor.forClass(FacilityEntity.class);

        // when
        FacilityResponseDto response = adminService.createFacility(requestDto);

        // then
        Mockito.verify(facilityRepository).save(captor.capture());
        FacilityEntity savedFacility = captor.getValue();

        Assertions.assertThat(savedFacility.getName()).isEqualTo("Hotel Spa");
        Assertions.assertThat(savedFacility.getFacilityType()).isEqualTo(FacilityEntity.FacilityType.HOTEL);

        Assertions.assertThat(response.facilityId()).isNull();
    }

    // createRoom

    @Test
    @DisplayName("Should successfully create a new room and publish event")
    void shouldCreateRoomSuccessfully() {
        // given
        long facilityId = 10L;
        RoomRequestDto roomRequestDto = new RoomRequestDto(101, 2, 250.0, RoomEntity.StandardType.NORMAL);

        FacilityEntity facilityEntity = new FacilityEntity();
        ReflectionTestUtils.setField(facilityEntity, "facilityId", facilityId);

        Mockito.when(facilityRepository.findById(facilityId))
                .thenReturn(Optional.of(facilityEntity));

        ArgumentCaptor<RoomEntity> roomCaptor = ArgumentCaptor.forClass(RoomEntity.class);
        ArgumentCaptor<RoomEventDto> eventCaptor = ArgumentCaptor.forClass(RoomEventDto.class);

        // when
        RoomResponseDto response = adminService.createRoom(facilityId, roomRequestDto);

        // then
        Mockito.verify(roomRepository).save(roomCaptor.capture());
        RoomEntity savedRoom = roomCaptor.getValue();

        Assertions.assertThat(savedRoom.getRoomNumber()).isEqualTo(101);
        Assertions.assertThat(savedRoom.getFacility().getFacilityId()).isEqualTo(facilityId);

        Mockito.verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        Assertions.assertThat(eventCaptor.getValue()).isNotNull();
    }

    @Test
    @DisplayName("Should throw RecordNotExistsDbException when creating room for non-existing facility")
    void shouldThrowExceptionWhenFacilityNotExistsForRoom() {
        // given
        long facilityId = 99L;
        RoomRequestDto roomRequestDto = new RoomRequestDto(101, 2, 250.0, RoomEntity.StandardType.NORMAL);

        Mockito.when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());

        // when & then
        Assertions.assertThatThrownBy(() -> adminService.createRoom(facilityId, roomRequestDto))
                .isInstanceOf(RecordNotExistsDbException.class)
                .hasMessageContaining("Cannot create room and assign to no exists facility");

        Mockito.verify(roomRepository, Mockito.never()).save(ArgumentMatchers.any());
        Mockito.verify(applicationEventPublisher, Mockito.never()).publishEvent(ArgumentMatchers.any());
    }

    // addEmployeeToFacility

    @Test
    @DisplayName("Should successfully add employee to facility and grant MANAGER role")
    void shouldAddEmployeeToFacilitySuccessfully() {
        // given
        long facilityId = 1L;
        long employeeId = 5L;

        FacilityEntity facilityEntity = new FacilityEntity();
        UserEntity userEntity = new UserEntity("employee@", null, null);
        userEntity.setRole(Role.ROLE_GUEST);

        Mockito.when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facilityEntity));
        Mockito.when(userRepository.findById(employeeId)).thenReturn(Optional.of(userEntity));

        // when
        adminService.addEmployeeToFacility(facilityId, employeeId);

        // then
        Assertions.assertThat(userEntity.getRole()).isEqualTo(Role.ROLE_MANAGER);
        Assertions.assertThat(facilityEntity.getEmployees()).contains(userEntity);
    }

    @Test
    @DisplayName("Should throw RecordNotExistsDbException when facility not found")
    void shouldThrowExceptionWhenFacilityNotFoundForEmployee() {
        long facilityId = 99L;
        long employeeId = 5L;

        Mockito.when(facilityRepository.findById(facilityId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> adminService.addEmployeeToFacility(facilityId, employeeId))
                .isInstanceOf(RecordNotExistsDbException.class)
                .hasMessageContaining("Cannot add employee to no exists facility");
    }

    @Test
    @DisplayName("Should throw RecordNotExistsDbException when user not found")
    void shouldThrowExceptionWhenUserNotFoundForFacility() {
        long facilityId = 1L;
        long employeeId = 99L;

        FacilityEntity facilityEntity = new FacilityEntity();

        Mockito.when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facilityEntity));
        Mockito.when(userRepository.findById(employeeId)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> adminService.addEmployeeToFacility(facilityId, employeeId))
                .isInstanceOf(RecordNotExistsDbException.class)
                .hasMessageContaining("Cannot add employee that not exists to facility");
    }
}
