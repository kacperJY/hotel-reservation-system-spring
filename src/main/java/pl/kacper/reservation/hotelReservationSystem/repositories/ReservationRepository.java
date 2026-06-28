package pl.kacper.reservation.hotelReservationSystem.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import pl.kacper.reservation.hotelReservationSystem.catalog.ReservationEntity;

import java.time.LocalDate;
import java.util.Optional;


public interface ReservationRepository extends ListCrudRepository<ReservationEntity, Long> {

    @EntityGraph(attributePaths = {"roomEntity", "roomEntity.facility"})
    Page<ReservationEntity> findByReservationOwner_UserId(Long reservationOwnerUserId, Pageable pageable);

    @EntityGraph(attributePaths = "reservationOwner")
    Optional<ReservationEntity> findWithUserByReservationId(Long reservationId);

    @EntityGraph(attributePaths = {"reservationOwner","roomEntity"})
    Optional<ReservationEntity> findFullReservationByReservationId(Long reservationId);

    @Query("select reservation from ReservationEntity reservation WHERE reservation.roomEntity.facility.facilityId =:facilityId")
    Page<ReservationEntity> findReservationByFacility(@Param("facilityId") Long facilityId, Pageable pageable);

    @EntityGraph(attributePaths = "roomEntity")
    Optional<ReservationEntity> findByReservationIdAndRoomEntity_Facility_FacilityId(Long reservationId, Long roomEntityFacilityFacilityId);

    @EntityGraph(attributePaths = "roomEntity")
    Slice<ReservationEntity> findByStatusAndCreatedAtBefore(ReservationEntity.Status status, LocalDate createdAtBefore, Pageable pageable);
}