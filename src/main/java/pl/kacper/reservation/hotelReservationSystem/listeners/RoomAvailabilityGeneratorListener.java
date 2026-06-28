package pl.kacper.reservation.hotelReservationSystem.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomAvailabilityEntity;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomEntity;
import pl.kacper.reservation.hotelReservationSystem.listeners.events.RoomEventDto;
import pl.kacper.reservation.hotelReservationSystem.repositories.RoomAvailabilityRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.RoomRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class RoomAvailabilityGeneratorListener {

    private final RoomRepository roomRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;

    @Value("${availability-days.range}")
    private int DAYS_TO_GENERATE;

    @Autowired
    public RoomAvailabilityGeneratorListener(RoomRepository roomRepository, RoomAvailabilityRepository roomAvailabilityRepository) {
        this.roomRepository = roomRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
    }

    @EventListener
    @Async
    public void generateAvailabilityRecords(RoomEventDto roomEventDto) {
        RoomEntity referenceRoom = roomRepository.getReferenceById(roomEventDto.roomId());

        List<RoomAvailabilityEntity> roomAvailabilityEntityList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < DAYS_TO_GENERATE; i++) {
            RoomAvailabilityEntity roomAvailabilityEntity = new RoomAvailabilityEntity(referenceRoom, today, 1);
            roomAvailabilityEntityList.add(roomAvailabilityEntity);
            today = today.plusDays(1);
        }
        roomAvailabilityRepository.saveAll(roomAvailabilityEntityList);
    }
}
