package pl.kacper.reservation.hotelReservationSystem.schduledServices;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomAvailabilityEntity;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomEntity;
import pl.kacper.reservation.hotelReservationSystem.repositories.RoomAvailabilityRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.RoomRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class RoomAvailabilityUpdaterService {

    @Value("${availability-days.range}")
    private int DAYS_TO_GENERATE;

    @Value("${database-fetch-size}")
    private int FETCH_SIZE;

    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final RoomRepository roomRepository;
    private final EntityManager entityManager;

    @Autowired
    public RoomAvailabilityUpdaterService(RoomAvailabilityRepository roomAvailabilityRepository, RoomRepository roomRepository, EntityManager entityManager) {
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.roomRepository = roomRepository;
        this.entityManager = entityManager;
    }


    @Scheduled(cron = "0 0 03 * * *", zone = "Europe/Poland")
    @Transactional
    public void renewAvailabilityRoomDates() {

        // DELETE outdated records
        LocalDate outdated = LocalDate.now().minusDays(1);
        roomAvailabilityRepository.deleteOutdatedAvailabilityDates(outdated);

        // new date
        LocalDate newDate = LocalDate.now().minusDays(1).plusDays(DAYS_TO_GENERATE);

        ArrayList<RoomAvailabilityEntity> roomAvailabilityEntityList = new ArrayList<>(FETCH_SIZE);

        List<Long> allRoomIds = roomRepository.findAllRoomIds();

        int counter = 0;
        for (Long roomId : allRoomIds) {
            RoomEntity roomReference = roomRepository.getReferenceById(roomId);
            RoomAvailabilityEntity roomAvailabilityEntity = new RoomAvailabilityEntity(roomReference, newDate, 1);
            roomAvailabilityEntityList.add(roomAvailabilityEntity);
            counter++;

            if(counter % FETCH_SIZE == 0){
                roomAvailabilityRepository.saveAll(roomAvailabilityEntityList);
                entityManager.flush();
                entityManager.clear();
                roomAvailabilityEntityList.clear();
            }
        }
        if(!roomAvailabilityEntityList.isEmpty())
            roomAvailabilityRepository.saveAll(roomAvailabilityEntityList);
    }

}
