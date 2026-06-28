package pl.kacper.reservation.hotelReservationSystem.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.kacper.reservation.hotelReservationSystem.repositories.FacilityRepository;

import java.util.Optional;

@Component("managerSecurity")
public class ManagerSecurityService {

    private final FacilityRepository facilityRepository;

    @Autowired
    public ManagerSecurityService(FacilityRepository facilityRepository) {
        this.facilityRepository = facilityRepository;
    }

    @Transactional(readOnly = true)
    public boolean checkFacilityAccess(UserDetails userDetails, Long facilityID){
        return  facilityRepository.existsByFacilityIdAndEmployees_Email(facilityID, userDetails.getUsername());

    }
}
