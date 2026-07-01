package pl.kacper.reservation.hotelReservationSystem.repositories;

import org.springframework.data.repository.ListCrudRepository;
import pl.kacper.reservation.hotelReservationSystem.catalog.CurrencyEntity;

import java.util.Optional;

public interface CurrencyRepository extends ListCrudRepository<CurrencyEntity, Long> {


    Optional<CurrencyEntity> findByCurrencyCode(String currencyCode);
}
