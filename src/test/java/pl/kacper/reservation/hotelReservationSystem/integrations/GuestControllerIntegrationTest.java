package pl.kacper.reservation.hotelReservationSystem.integrations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import pl.kacper.reservation.hotelReservationSystem.guest.dtos.ReservationRequestDto;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class GuestControllerIntegrationTest extends BaseIntegrationTest {


    @Test
    @Sql(scripts = {"classpath:/sql/scripts/insert_hotel_with_room_and_availabilities.sql", "classpath:sql/scripts/insert_user.sql"})
    @WithMockUser(username = "jan.kowalski@example.com", password = "password123", roles = "GUEST")
    void shouldSuccessfullyBookAvailableRoom() throws Exception {
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(1L, 1L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(10));

        String jsonDtoRequest = objectMapper.writeValueAsString(reservationRequestDto);

        mockMvc.perform(post("/api/guests/rooms/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonDtoRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationId").exists());
    }

    @Test
    @WithMockUser(username = "jan.kowalski@example.com", password = "password123", roles = "GUEST")
    void shouldRejectReservationWhenEndDateIsNotAfterStartDate() throws Exception {
        LocalDate date = LocalDate.now().plusDays(5);
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(1L, 1L, date, date);

        mockMvc.perform(post("/api/guests/rooms/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequestDto))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.dateRangeValid").value("endDate must be after startDate"));
    }

    @Test
    @WithMockUser(username = "jan.kowalski@example.com", password = "password123", roles = "GUEST")
    void shouldRejectReservationWhenStartDateIsInThePast() throws Exception {
        ReservationRequestDto reservationRequestDto = new ReservationRequestDto(1L, 1L, LocalDate.now().minusDays(1), LocalDate.now().plusDays(3));

        mockMvc.perform(post("/api/guests/rooms/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequestDto))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.startDate").exists());
    }
}
