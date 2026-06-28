package pl.kacper.reservation.hotelReservationSystem.schduledServices;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.kacper.reservation.hotelReservationSystem.catalog.ReservationEntity;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomAvailabilityEntity;
import pl.kacper.reservation.hotelReservationSystem.repositories.ReservationRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.RoomAvailabilityRepository;

import java.time.LocalDate;
import java.util.List;

@Component
public class PendingReservationAfterTimeUpdaterService {

    private final ReservationRepository reservationRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final EntityManager entityManager;

    @Value("${cancel-reservation-after-days}")
    private int daysAfterCancelReservation;

    @Value("${database-fetch-size}")
    private int fetchSize;

    public PendingReservationAfterTimeUpdaterService(ReservationRepository reservationRepository, RoomAvailabilityRepository roomAvailabilityRepository, EntityManager entityManager) {
        this.reservationRepository = reservationRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.entityManager = entityManager;
    }

    @Scheduled(cron = "0 0 03 * * *", zone = "Europe/Poland")
    @Transactional
    public void updateOldPendingReservations() {

        Pageable pageable = PageRequest.of(0, fetchSize);

        Slice<ReservationEntity> reservationEntitySlice;

        LocalDate cutOffDate = LocalDate.now().minusDays(daysAfterCancelReservation);

        do {
            reservationEntitySlice = reservationRepository.findByStatusAndCreatedAtBefore(ReservationEntity.Status.PENDING, cutOffDate, pageable);

            List<ReservationEntity> content = reservationEntitySlice.getContent();

            if (content.isEmpty()) break;

            for (ReservationEntity reservationEntity : content) {

                reservationEntity.setStatus(ReservationEntity.Status.CANCELLED);

                List<RoomAvailabilityEntity> takenByDatesAndRoomId = roomAvailabilityRepository.findTakenByDatesAndRoomId(
                        reservationEntity.getRoomEntity().getRoomId(),
                        reservationEntity.getCheckIn(),
                        reservationEntity.getCheckOut());
                takenByDatesAndRoomId.forEach(roomAvailabilityEntity -> roomAvailabilityEntity.setFreeSlots(1));
            }

            entityManager.flush();

            entityManager.clear();

        } while (reservationEntitySlice.hasNext());
    }
}
