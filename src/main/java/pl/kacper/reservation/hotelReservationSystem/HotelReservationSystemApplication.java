package pl.kacper.reservation.hotelReservationSystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@EnableAsync
public class HotelReservationSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(HotelReservationSystemApplication.class, args);
	}

}
