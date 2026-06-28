package pl.kacper.reservation.hotelReservationSystem.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomEntity;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<RoomEntity, Long> {

    @Query("select r.roomId from RoomEntity r")
    List<Long> findAllRoomIds();

    @Query("""
            SELECT room.roomId FROM RoomEntity room
            JOIN room.facility fc
            JOIN RoomAvailabilityEntity ra on ra.roomEntity.roomId = room.roomId
            WHERE ra.date >= :startDate AND ra.date < :endDate
            AND ra.freeSlots = 1
            AND (:city IS NULL OR fc.address.city=:city)
            AND (:roomCapacity IS NULL OR room.roomCapacity=:roomCapacity)
            GROUP BY room.roomId
            HAVING count(*) >= :nights
            """
    )
    List<Long> findRoomsIdWithFilter(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("nights") long nights,
            @Param("city") String city,
            @Param("roomCapacity") Integer roomCapacity
    );

    Optional<RoomEntity> findByRoomIdAndFacility_FacilityId(Long roomId, Long facilityFacilityId);


    @Query("SELECT room FROM RoomEntity room JOIN FETCH room.facility WHERE room.roomId IN (:roomIds)")
    List<RoomEntity> findRoomsWithFacilityByIds(@Param("roomIds") List<Long> roomIds);

}
