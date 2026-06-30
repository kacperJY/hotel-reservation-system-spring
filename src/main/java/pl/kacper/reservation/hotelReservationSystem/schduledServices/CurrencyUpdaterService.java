package pl.kacper.reservation.hotelReservationSystem.services;

import jakarta.annotation.PostConstruct;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pl.kacper.reservation.hotelReservationSystem.dtos.CurrenciesTableDto;
import pl.kacper.reservation.hotelReservationSystem.repositories.CurrencyRepository;

import java.net.http.HttpClient;

@Service
public class CurrencyService {

    private final HttpClient httpClient;
    private final CurrencyRepository currencyRepository;

    private RestClient restClient;

    public CurrencyService(HttpClient httpClient, CurrencyRepository currencyRepository) {
        this.httpClient = httpClient;
        this.currencyRepository = currencyRepository;
    }

    @PostConstruct
    public void init() {
        this.restClient = RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .baseUrl("https://api.nbp.pl/api/exchangerates/tables/A/") // https://api.nbp.pl/api/exchangerates/tables/{table}/ --> {table} == A
                .build();

        System.out.println("ZAINICJOWANO RESTCLIENTA ####");
    }

    public void fetchCurrencies() {
        CurrenciesTableDto[] currenciesTableDtos = restClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(CurrenciesTableDto[].class);


    }
}