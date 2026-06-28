package pl.kacper.reservation.hotelReservationSystem.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.kacper.reservation.hotelReservationSystem.catalog.FacilityEntity;

import java.util.Optional;

public interface FacilityRepository extends JpaRepository<FacilityEntity, Long> {


    boolean existsByFacilityIdAndEmployees_Email(Long facilityId, String email);

}
