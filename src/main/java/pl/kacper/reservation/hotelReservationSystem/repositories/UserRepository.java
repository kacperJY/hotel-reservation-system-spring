package pl.kacper.reservation.hotelReservationSystem.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.kacper.reservation.hotelReservationSystem.user.Role;
import pl.kacper.reservation.hotelReservationSystem.user.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepository extends ListCrudRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    @Modifying(clearAutomatically = true,flushAutomatically = true)
    @Query("update UserEntity user SET user.role=:role WHERE user.userId=:userId")
    int updateRole(@Param("userId") Long userId, @Param("role") Role role);
}
