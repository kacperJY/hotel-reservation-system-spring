package pl.kacper.reservation.hotelReservationSystem.exceptionHandler;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.kacper.reservation.hotelReservationSystem.exception.ApiResponseException;
import pl.kacper.reservation.hotelReservationSystem.exception.NoElementsException;
import pl.kacper.reservation.hotelReservationSystem.exception.RecordNotExistsDbException;
import pl.kacper.reservation.hotelReservationSystem.exception.RoomAlreadyReservedException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({RecordNotExistsDbException.class, NoElementsException.class, RoomAlreadyReservedException.class, IllegalArgumentException.class, IllegalStateException.class, ApiResponseException.class})
    public ResponseEntity<String> handleRecordNotExistsInDb(Throwable throwable) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(throwable.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(BindingResult bindingResult) {

        Map<String, @Nullable String> errorMap = bindingResult.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (msg1, msg2) -> msg1 + msg2
                ));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMap);
    }
}
