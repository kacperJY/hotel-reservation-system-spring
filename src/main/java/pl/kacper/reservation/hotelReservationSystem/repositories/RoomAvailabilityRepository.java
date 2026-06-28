package pl.kacper.reservation.hotelReservationSystem.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomAvailabilityEntity;

import java.time.LocalDate;
import java.util.List;

public interface RoomAvailabilityRepository extends ListCrudRepository<RoomAvailabilityEntity, Long> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RoomAvailabilityEntity ra where ra.date=:oldDate")
    int deleteOutdatedAvailabilityDates(@Param("oldDate") LocalDate oldDate);

    @Query("SELECT ra FROM RoomAvailabilityEntity ra WHERE ra.roomEntity.roomId=:roomId AND ra.date >= :startDate AND ra.date < :endDate AND ra.freeSlots=1")
    List<RoomAvailabilityEntity> findAvailabilityByDatesAndRoomId(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT ra FROM RoomAvailabilityEntity ra WHERE ra.roomEntity.roomId=:roomId AND ra.date >= :startDate AND ra.date < :endDate AND ra.freeSlots=0")
    List<RoomAvailabilityEntity> findTakenByDatesAndRoomId(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
