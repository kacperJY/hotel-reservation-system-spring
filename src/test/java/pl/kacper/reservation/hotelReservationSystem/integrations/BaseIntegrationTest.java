package pl.kacper.reservation.hotelReservationSystem.integrations;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class BaseIntegrationTest {

    @ServiceConnection
    private static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    private static WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    static {
        postgreSQLContainer.start();
        wireMockServer.start();

        wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/api/exchangerates/tables/A/"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        // Minimalny, poprawny JSON wymagany przez Twój parser w serwisie
                        .withBody("""
                                [
                                  {
                                    "table": "A",
                                    "no": "128/A/NBP/2026",
                                    "effectiveDate": "2026-07-03",
                                    "rates": [
                                      {
                                        "currency": "dolar amerykański",
                                        "code": "USD",
                                        "mid": 3.9542
                                      },
                                      {
                                        "currency": "euro",
                                        "code": "EUR",
                                        "mid": 4.2815
                                      }
                                    ]
                                  }
                                ]
                                """)
                ));
    }

    @AfterEach
    public void cleanDatabase() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "currencies", "reservations", "room_availability", "rooms", "facilities");
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("nbp-api-url", () -> wireMockServer.baseUrl() + "/api/exchangerates/tables/A/");
    }

}
