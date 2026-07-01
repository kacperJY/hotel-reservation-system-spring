package pl.kacper.reservation.hotelReservationSystem.manager;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.kacper.reservation.hotelReservationSystem.catalog.ReservationEntity;
import pl.kacper.reservation.hotelReservationSystem.exception.NoElementsException;
import pl.kacper.reservation.hotelReservationSystem.manager.dtos.ReservationStatusRequestDto;
import pl.kacper.reservation.hotelReservationSystem.repositories.ReservationRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.RoomAvailabilityRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {


    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomAvailabilityRepository roomAvailabilityRepository;

    @InjectMocks
    private ManagerService managerService;


    @Test
    @DisplayName("Should throw IllegalArgumentException when page number is < 1")
    void shouldThrowExceptionWhenInvalidPageNumber() {
        long facilityId = 1;

        int wrongPageNumber = 0;

        Assertions.assertThatThrownBy(() -> managerService.getAllReservationsByFacility(facilityId, wrongPageNumber))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid page number.");
    }

    @Test
    @DisplayName("Should throw NoElementsException when there are not any reservation for passed facility")
    void shouldThrowExceptionWhenNoReservationForFacility() {

        long facilityId = 1;

        Mockito.when(reservationRepository.findReservationByFacility(ArgumentMatchers.eq(facilityId), ArgumentMatchers.any()))
                .thenReturn(Page.empty());

        Assertions.assertThatThrownBy(() -> managerService.getAllReservationsByFacility(facilityId, 1))
                .isInstanceOf(NoElementsException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ReservationEntity.Status.class, mode = EnumSource.Mode.EXCLUDE, names = "CANCELLED")
    @DisplayName("Should be false when transitioning from CANCELED to ANYTHING")
    void shouldCancelToAnythingReturnFalse(ReservationEntity.Status nextStatus) {

        ReservationEntity.Status currentStatus = ReservationEntity.Status.CANCELLED;

        boolean result = managerService.validStatusFlow(currentStatus, nextStatus);

        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ReservationEntity.Status.class, mode = EnumSource.Mode.EXCLUDE, names = "COMPLETED")
    @DisplayName("Should be false when transitioning from COMPLETED to ANYTHING")
    void shouldCompletedToAnythingReturnFalse(ReservationEntity.Status nextStatus) {

        ReservationEntity.Status currentStatus = ReservationEntity.Status.COMPLETED;

        boolean result = managerService.validStatusFlow(currentStatus, nextStatus);

        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ReservationEntity.Status.class, mode = EnumSource.Mode.INCLUDE, names = {"CONFIRMED", "CANCELLED"})
    @DisplayName("Should be true when transitioning from PENDING to CONFIRMED or CANCELED")
    void shouldPendingToConfirmedOrCanceledReturnTrue(ReservationEntity.Status nextStatus) {

        ReservationEntity.Status currentStatus = ReservationEntity.Status.PENDING;

        boolean result = managerService.validStatusFlow(currentStatus, nextStatus);

        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ReservationEntity.Status.class, mode = EnumSource.Mode.INCLUDE, names = {"IN_PROGRESS", "CANCELLED"})
    @DisplayName("Should be true when transitioning from CONFIRMED to IN_PROGRESS or CANCELED")
    void shouldConfirmedToInProgressOrCanceledReturnTrue(ReservationEntity.Status nextStatus) {

        ReservationEntity.Status currentStatus = ReservationEntity.Status.CONFIRMED;

        boolean result = managerService.validStatusFlow(currentStatus, nextStatus);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should be true when transitioning from IN_PROGRESS to COMPLETED")
    void shouldInProgressToCompletedReturnTrue() {

        ReservationEntity.Status currentStatus = ReservationEntity.Status.IN_PROGRESS;
        ReservationEntity.Status nextStatus = ReservationEntity.Status.COMPLETED;

        boolean result = managerService.validStatusFlow(currentStatus, nextStatus);

        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ReservationEntity.Status.class, mode = EnumSource.Mode.EXCLUDE, names = {"CONFIRMED", "CANCELLED"})
    @DisplayName("Should return false when transitioning from PENDING to invalid states")
    void shouldPendingToInvalidStatesReturnFalse(ReservationEntity.Status nextStatus) {
        ReservationEntity.Status currentStatus = ReservationEntity.Status.PENDING;
        boolean result = managerService.validStatusFlow(currentStatus, nextStatus);

        assertThat(result).isFalse();
    }


    @Test
    @DisplayName("Should successfully change reservation status from PENDING to CONFIRMED")
    void shouldUpdateReservationStatusToConfirmed() {
        Long facilityId = 1L;
        Long reservationId = 100L;
        ReservationStatusRequestDto requestDto = new ReservationStatusRequestDto(ReservationEntity.Status.CONFIRMED);

        ReservationEntity mockReservation = new ReservationEntity(null, null, BigDecimal.ZERO, ReservationEntity.Status.PENDING, null, null);

        Mockito.when(reservationRepository.findByReservationIdAndRoomEntity_Facility_FacilityId(reservationId, facilityId))
                .thenReturn(Optional.of(mockReservation));

        managerService.updateReservationStatus(facilityId, reservationId, requestDto);

        assertThat(mockReservation.getStatus()).isEqualTo(ReservationEntity.Status.CONFIRMED);
    }

    @Test
    @DisplayName("Should throw a IllegalStateException when cannot find reservation in DB")
    void shouldThrowExceptionWhenNotFoundReservation() {

        Mockito.when(reservationRepository.findByReservationIdAndRoomEntity_Facility_FacilityId(ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        ReservationStatusRequestDto dummyDto = new ReservationStatusRequestDto(ReservationEntity.Status.CONFIRMED);

        Assertions.assertThatThrownBy(() -> managerService.updateReservationStatus(0L, 0L, dummyDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("This reservations not belongs to picked facility");
    }

    @Test
    @DisplayName("Should throw a IllegalStateException when reservation status flow is invalid")
    void shouldThrowExceptionWhenStatusFlowIsInvalidReservation() {

        long reservationId = 1L;
        long facilityId = 10L;

        Mockito.when(reservationRepository.findByReservationIdAndRoomEntity_Facility_FacilityId(reservationId, facilityId))
                .thenReturn(Optional.of(new ReservationEntity(null, null, BigDecimal.ZERO, ReservationEntity.Status.CANCELLED, null, null)));

        ReservationStatusRequestDto dummyDto = new ReservationStatusRequestDto(ReservationEntity.Status.CONFIRMED);

        // Cancel cannot transit to anything else
        Assertions.assertThatThrownBy(() -> managerService.updateReservationStatus(facilityId, reservationId, dummyDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot change status");
    }
}
