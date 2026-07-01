package pl.kacper.reservation.hotelReservationSystem.schduledServices;

import jakarta.annotation.PostConstruct;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
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

    private RestClient restClient;

    private static final String API_BASE_URL = "https://api.nbp.pl/api/exchangerates/tables/A/";

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
    public void fetchCurrencies() {
        CurrenciesTableDto[] currenciesTableDtos = restClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    throw new ClientApiRequestException("Invalid api request to: %s %n Error info: %s - %s".formatted(API_BASE_URL, response.getStatusCode(), response.getStatusText()));
                })
                .onStatus(HttpStatusCode::is5xxServerError, ((request, response) -> {
                    throw new ApiResponseException("Api error from: %s %n Error info: %s - %s".formatted(API_BASE_URL, response.getStatusCode(), response.getStatusText()));
                }))
                .body(CurrenciesTableDto[].class);

        if (currenciesTableDtos == null) {
            throw new FetchNoResultException("There is no result from API: " + API_BASE_URL);
        }

        List<CurrencyEntity> currencyEntityList = new ArrayList<>();
        for (CurrenciesTableDto currenciesTableDto : currenciesTableDtos) {
            for (CurrencyDto currencyDto : currenciesTableDto.currencyDtoList()) {
                currencyEntityList.add(
                        new CurrencyEntity(
                                currencyDto.currencyName(),
                                currencyDto.currencyCode(),
                                currencyDto.averageRate(),
                                currenciesTableDto.publicationData()
                        )
                );
            }
        }
        Cache currencyRatesCache = cacheManager.getCache("currencyRates");
        if (currencyRatesCache != null) currencyRatesCache.clear();

        currencyRepository.saveAll(currencyEntityList);
    }
}