package pl.kacper.reservation.hotelReservationSystem.schduledServices;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import pl.kacper.reservation.hotelReservationSystem.catalog.CurrencyEntity;
import pl.kacper.reservation.hotelReservationSystem.dtos.CurrenciesTableDto;
import pl.kacper.reservation.hotelReservationSystem.dtos.CurrencyDto;
import pl.kacper.reservation.hotelReservationSystem.exception.ApiResponseException;
import pl.kacper.reservation.hotelReservationSystem.exception.ClientApiRequestException;
import pl.kacper.reservation.hotelReservationSystem.exception.FetchNoResultException;
import pl.kacper.reservation.hotelReservationSystem.repositories.CurrencyRepository;

import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

@Component
public class CurrencyUpdaterService {

    private final HttpClient httpClient;
    private final CurrencyRepository currencyRepository;
    private final CacheManager cacheManager;

    private final Logger logger = LoggerFactory.getLogger(CurrencyUpdaterService.class);

    private RestClient restClient;

    @Value("${nbp-api-url}")
    private String API_BASE_URL;

    public CurrencyUpdaterService(HttpClient httpClient, CurrencyRepository currencyRepository, CacheManager cacheManager) {
        this.httpClient = httpClient;
        this.currencyRepository = currencyRepository;
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .baseUrl(API_BASE_URL) // https://api.nbp.pl/api/exchangerates/tables/{table}/ --> {table} == A
                .build();
    }

    @Scheduled(cron = "0 0 03 * * *", zone = "Europe/Poland")
    @Transactional
    @Async
    public void fetchCurrencies() {
        CurrenciesTableDto[] currenciesTableDtos = restClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    logger.error("Invalid api request to: {} \n Error info: {} - {}", API_BASE_URL, response.getStatusCode(), response.getStatusText());
                })
                .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                    logger.error("API not available: {} %n Error info: {} - {}", API_BASE_URL, response.getStatusCode(), response.getStatusText());
                    logger.info("Service will use last fetched data from NBP");
                }))
                .onStatus(HttpStatusCode::is2xxSuccessful, ((request, response) -> System.out.println(response.getBody())))
                .onStatus(HttpStatusCode::is1xxInformational, ((request, response) -> System.out.println(response.getBody())))
                .body(CurrenciesTableDto[].class);


        if(currenciesTableDtos == null) {
            currencyRepository.findFirstByCurrencyCode("EUR").orElseThrow(() -> new ApiResponseException("FATAL: Cannot initialize currencies"));
            return;
        }

        List<CurrencyEntity> currencyEntityList = new ArrayList<>();
        for (CurrenciesTableDto currenciesTableDto : currenciesTableDtos) {
            for (CurrencyDto currencyDto : currenciesTableDto.currencyDtoList()) {
                currencyRepository.findByCurrencyCode(currencyDto.currencyCode()).ifPresentOrElse(
                        existingCurrency -> {
                            existingCurrency.setAverageRate(currencyDto.averageRate());
                            existingCurrency.setPublicationDate(currenciesTableDto.publicationData());
                        },
                        () -> currencyEntityList.add(new CurrencyEntity(currencyDto.currencyName(), currencyDto.currencyCode(), currencyDto.averageRate(), currenciesTableDto.publicationData()))

                );
            }
        }
        Cache currencyRatesCache = cacheManager.getCache("currencyRates");
        if (currencyRatesCache != null) currencyRatesCache.clear();

        currencyRepository.saveAll(currencyEntityList);
    }
}