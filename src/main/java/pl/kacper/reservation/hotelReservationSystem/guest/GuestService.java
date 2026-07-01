package pl.kacper.reservation.hotelReservationSystem.guest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kacper.reservation.hotelReservationSystem.catalog.CurrencyEntity;
import pl.kacper.reservation.hotelReservationSystem.catalog.ReservationEntity;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomAvailabilityEntity;
import pl.kacper.reservation.hotelReservationSystem.catalog.RoomEntity;
import pl.kacper.reservation.hotelReservationSystem.dtos.PageResultDto;
import pl.kacper.reservation.hotelReservationSystem.exception.NoElementsException;
import pl.kacper.reservation.hotelReservationSystem.exception.RecordNotExistsDbException;
import pl.kacper.reservation.hotelReservationSystem.exception.RoomAlreadyReservedException;
import pl.kacper.reservation.hotelReservationSystem.guest.dtos.ReservationHistoryResponseDto;
import pl.kacper.reservation.hotelReservationSystem.guest.dtos.ReservationRequestDto;
import pl.kacper.reservation.hotelReservationSystem.dtos.ReservationResponseDto;
import pl.kacper.reservation.hotelReservationSystem.guest.dtos.RoomResultDto;
import pl.kacper.reservation.hotelReservationSystem.repositories.ReservationRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.RoomAvailabilityRepository;
import pl.kacper.reservation.hotelReservationSystem.repositories.RoomRepository;
import pl.kacper.reservation.hotelReservationSystem.services.CurrencyService;
import pl.kacper.reservation.hotelReservationSystem.user.UserEntity;
import pl.kacper.reservation.hotelReservationSystem.repositories.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class GuestService {

    private final RoomRepository roomRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final CurrencyService currencyService;

    public GuestService(RoomRepository roomRepository, RoomAvailabilityRepository roomAvailabilityRepository, ReservationRepository reservationRepository, UserRepository userRepository, CurrencyService currencyService) {
        this.roomRepository = roomRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.currencyService = currencyService;
    }

    public List<RoomResultDto> findRooms(String city, Integer guestNumber, String currency, LocalDate startDate, LocalDate endDate) {

        long nights = ChronoUnit.DAYS.between(startDate, endDate);
        if (nights <= 0) {
            return Collections.emptyList();
        }

        List<Long> roomsIdWithFilter = roomRepository.findRoomsIdWithFilter(startDate, endDate, nights, city, guestNumber);

        List<RoomEntity> roomsWithFacilityByIds = roomRepository.findRoomsWithFacilityByIds(roomsIdWithFilter);

        List<RoomResultDto> roomResultDtoList = new ArrayList<>();

        for (RoomEntity roomEntity : roomsWithFacilityByIds) {
            BigDecimal fullPrice = calculateFullPrice(nights, roomEntity.getPricePerNight(), currency);

            roomResultDtoList.add(new RoomResultDto(
                    roomEntity.getRoomId(),
                    roomEntity.getFacility().getFacilityId(),
                    roomEntity.getFacility().getName(),
                    roomEntity.getFacility().getDescription(),
                    roomEntity.getFacility().getAmenities(),
                    roomEntity.getFacility().getFacilityType(),
                    roomEntity.getFacility().getAddress(),
                    roomEntity.getRoomCapacity(),
                    convertToCurrency(roomEntity.getPricePerNight(),currency),
                    fullPrice,
                    currency,
                    nights,
                    roomEntity.getStandardType()
            ));
        }

        return roomResultDtoList;
    }

    @Transactional
    public ReservationResponseDto createReservation(ReservationRequestDto requestDto, UserDetails userDetails) {

        LocalDate start = requestDto.startDate();
        LocalDate end = requestDto.endDate();
        long nights = ChronoUnit.DAYS.between(start, end);

        RoomEntity roomEntity = roomRepository.findByRoomIdAndFacility_FacilityId(requestDto.roomId(), requestDto.facilityId())
                .orElseThrow(() -> new RecordNotExistsDbException("Cannot reserve room that not exists in this facility"));

        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("You have to be login to create reservation"));

        // DIRTY CHECKING - LOCK OPTIMISTIC - SET ROOM DATES UNAVAILABILITY
        List<RoomAvailabilityEntity> availabilityByDatesAndRoomIdList = roomAvailabilityRepository.findAvailabilityByDatesAndRoomId(roomEntity.getRoomId(), start, end);
        if (availabilityByDatesAndRoomIdList.size() < nights)
            throw new RoomAlreadyReservedException("Cannot reserve chosen room because this room is already reserved");
        else
            availabilityByDatesAndRoomIdList.forEach(availability -> availability.setFreeSlots(0));


        BigDecimal fullPrice = calculateFullPrice(nights, roomEntity.getPricePerNight());

        ReservationEntity reservationEntity = new ReservationEntity(
                start,
                end,
                fullPrice,
                ReservationEntity.Status.PENDING,
                roomEntity,
                userEntity
        );

        reservationRepository.save(reservationEntity);

        return new ReservationResponseDto(
                reservationEntity.getReservationId(),
                reservationEntity.getStatus(),
                reservationEntity.getCheckIn(),
                reservationEntity.getCheckOut(),
                fullPrice
        );
    }

    BigDecimal convertToCurrency(BigDecimal value, String currencyCode) {
        if (currencyCode.equals("PLN")) return value;

        CurrencyEntity currencyRate = currencyService.getCurrencyRate(currencyCode);
        BigDecimal averageRate = currencyRate.getAverageRate();

        return value.divide(averageRate, 2, RoundingMode.HALF_UP);
    }

    BigDecimal calculateFullPrice(long nights, BigDecimal roomPricePerNight) {
        return BigDecimal.valueOf(nights).multiply(roomPricePerNight);
    }

    BigDecimal calculateFullPrice(long nights, BigDecimal roomPricePerNight, String currencyCode) {
        if (!currencyCode.equals("PLN")) {
            CurrencyEntity currencyRate = currencyService.getCurrencyRate(currencyCode);
            BigDecimal averageRate = currencyRate.getAverageRate();
            return (roomPricePerNight.multiply(BigDecimal.valueOf(nights))).divide(averageRate, 2, RoundingMode.HALF_UP);
        }

        return BigDecimal.valueOf(nights).multiply(roomPricePerNight);
    }


    public PageResultDto<ReservationHistoryResponseDto> getUserReservationHistory(UserDetails userDetails, int originalPageNumber) {
        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new UsernameNotFoundException("You have to be login to create reservation"));

        int pageNumber = originalPageNumber - 1;  // PAGEABLE start counting from 0
        if (originalPageNumber < 1) throw new IllegalArgumentException("Invalid page number. Minimal page number is 1");

        Sort sort = Sort.by("checkIn").ascending();
        PageRequest reservationPageable = PageRequest.of(pageNumber, 10, sort);

        Page<ReservationEntity> reservationPage = reservationRepository.findByReservationOwner_UserId(userEntity.getUserId(), reservationPageable);

        long totalElements = reservationPage.getTotalElements();
        int maxPages = reservationPage.getTotalPages();
        int elementsOnPage = reservationPage.getNumberOfElements();

        if (totalElements == 0)
            throw new NoElementsException("There are no reservation for this user");
        else if (elementsOnPage == 0)
            throw new IllegalArgumentException("Invalid page number %d/%d".formatted(originalPageNumber, maxPages));

        List<ReservationHistoryResponseDto> reservationHistoryResponseDtoList = new ArrayList<>();

        for (ReservationEntity reservationEntity : reservationPage.getContent()) {
            long nights = ChronoUnit.DAYS.between(reservationEntity.getCheckIn(), reservationEntity.getCheckOut());
            ReservationHistoryResponseDto.ReservationHistoryStatus reservationHistoryStatus = reservationEntity.getCheckIn().isBefore(LocalDate.now())
                    ? ReservationHistoryResponseDto.ReservationHistoryStatus.PAST
                    : ReservationHistoryResponseDto.ReservationHistoryStatus.FUTURE;

            reservationHistoryResponseDtoList.add(
                    new ReservationHistoryResponseDto(
                            reservationEntity.getReservationId(),
                            reservationEntity.getCheckIn(),
                            reservationEntity.getCheckOut(),
                            nights,
                            reservationEntity.getStatus(),
                            reservationEntity.getFullPrice(),
                            reservationEntity.getRoomEntity().getFacility().getName(),
                            reservationEntity.getRoomEntity().getFacility().getAddress(),
                            reservationEntity.getRoomEntity().getRoomCapacity(),
                            reservationHistoryStatus
                    )
            );
        }

        return new PageResultDto<>(
                originalPageNumber,
                maxPages,
                totalElements,
                elementsOnPage,
                reservationHistoryResponseDtoList
        );
    }

    @Transactional
    public void cancelReservation(Long reservationId, UserDetails userDetails) {
        ReservationEntity reservationEntity = reservationRepository.findFullReservationByReservationId(reservationId)
                .orElseThrow(() -> new RecordNotExistsDbException("Cannot cancel reservation that not exists"));

        if (!reservationEntity.getReservationOwner().getEmail().equals(userDetails.getUsername()))
            throw new AccessDeniedException("Cannot get access to someone else reservation");

        LocalDate checkIn = reservationEntity.getCheckIn();
        LocalDate checkOut = reservationEntity.getCheckOut();
        Long roomId = reservationEntity.getRoomEntity().getRoomId();

        long between = ChronoUnit.DAYS.between(LocalDate.now(), checkIn);
        if (between < 2)
            throw new IllegalStateException("Reservations cannot be cancelled less than 2 days before the reservation date");
        if (reservationEntity.getStatus() != ReservationEntity.Status.PENDING)
            throw new IllegalStateException("Only PENDING reservations can be cancelled.");

        List<RoomAvailabilityEntity> availabilityByDatesAndRoomId = roomAvailabilityRepository.findTakenByDatesAndRoomId(roomId, checkIn, checkOut);
        availabilityByDatesAndRoomId.forEach(takenDates -> takenDates.setFreeSlots(1));

        reservationEntity.setStatus(ReservationEntity.Status.CANCELLED);
    }


}
