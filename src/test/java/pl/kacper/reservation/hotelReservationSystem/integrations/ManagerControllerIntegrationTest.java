package pl.kacper.reservation.hotelReservationSystem.integrations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.jdbc.Sql;
import pl.kacper.reservation.hotelReservationSystem.security.JWTService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ManagerControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    @Sql(scripts = {"classpath:sql/scripts/insert_hotel_with_room_and_availabilities.sql", "classpath:sql/scripts/insert_user.sql"})
    void shouldRejectFacilityReservationsRequestWhenUserIsGuest() throws Exception {

        UserDetails userDetails = userDetailsService.loadUserByUsername("jan.kowalski@example.com");

        String token = jwtService.generateToken(userDetails);
        String fullToken = "Bearer " + token;

        mockMvc.perform(get("/api/managers/facilities/1/reservations")
                        .header("Authorization", fullToken)
                )
                .andExpect(status().isForbidden());
    }
}
