package pl.kacper.reservation.hotelReservationSystem.services;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.kacper.reservation.hotelReservationSystem.catalog.CurrencyEntity;
import pl.kacper.reservation.hotelReservationSystem.exception.RecordNotExistsDbException;
import pl.kacper.reservation.hotelReservationSystem.repositories.CurrencyRepository;
import pl.kacper.reservation.hotelReservationSystem.schduledServices.CurrencyUpdaterService;

@Service
@Transactional(readOnly = true)
public class CurrencyService {

    private final CurrencyRepository currencyRepository;
    private final CurrencyUpdaterService currencyUpdaterService;

    private final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

    public CurrencyService(CurrencyRepository currencyRepository, CurrencyUpdaterService currencyUpdaterService) {
        this.currencyRepository = currencyRepository;
        this.currencyUpdaterService = currencyUpdaterService;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional // new transaction for modifying operation
    public void init() {
        try {
            currencyUpdaterService.fetchCurrencies();
        } catch (Exception e) {
            logger.info("Couldn't fetch new currency rates to update current state: {}", e.getMessage());
        }
    }

    @Cacheable(value = "currencyRates", key = "#currencyCode", sync = true)
    public CurrencyEntity getCurrencyRate(String currencyCode) {
        return currencyRepository.findByCurrencyCode(currencyCode).orElseThrow(() -> new RecordNotExistsDbException("Currency with typed currency code does not exists"));
    }
}
