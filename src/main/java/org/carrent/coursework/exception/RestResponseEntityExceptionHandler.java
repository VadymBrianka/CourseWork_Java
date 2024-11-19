package org.carrent.coursework.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { CarNotFoundException.class,
            CustomerNotFoundException.class,
            EmployeeNotFoundException.class,
            OrderNotFoundException.class,
            ServiceOfCarNotFoundException.class,
            CarAlreadyExistsException.class ,
            CustomerAlreadyExistsException.class,
            EmployeeAlreadyExistsException.class ,
            OrderAlreadyExistsException.class,
            ServiceOfCarAlreadyExistsException.class,
            InvalidEmployeePositionException.class,
            EmployeePositionNotAllowedException.class,
            CarNotAvailableException.class})
    protected ResponseEntity<Object> handleNotFoundException(RuntimeException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();

        // Форматуємо timestamp в ISO 8601
        String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        body.put("timestamp", timestamp);

        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", "")); // Отримання шляху запиту

        return handleExceptionInternal(ex, body, new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
}
