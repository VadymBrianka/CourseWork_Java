package org.carrent.coursework.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.ServiceOfCarCreationDto;
import org.carrent.coursework.dto.ServiceOfCarDto;
import org.carrent.coursework.enums.ServiceOfCarStatus;
import org.carrent.coursework.service.ServiceOfCarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/services")
@AllArgsConstructor
public class ServiceOfCarController {

    private final ServiceOfCarService serviceOfCarService;

    @GetMapping("{id}")
    public ResponseEntity<ServiceOfCarDto> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceOfCarService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<ServiceOfCarDto>> getAllServices(Pageable pageable) {
        // Використовуємо пагінацію для отримання сервісів автомобілів
        return ResponseEntity.ok(serviceOfCarService.getAll(pageable));
    }

    @PostMapping
    public ResponseEntity<ServiceOfCarDto> createService(@Valid @RequestBody ServiceOfCarCreationDto serviceOfCarCreationDto) {
        // Створюємо новий сервіс автомобіля
        return new ResponseEntity<>(serviceOfCarService.create(serviceOfCarCreationDto), HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<ServiceOfCarDto> updateService(@PathVariable Long id, @Valid @RequestBody ServiceOfCarDto serviceOfCarDto) {
        // Оновлюємо сервіс автомобіля за ID
        ServiceOfCarDto updatedService = serviceOfCarService.updateServiceOfCar(id, serviceOfCarDto);
        return ResponseEntity.ok(updatedService);
    }

    @GetMapping("/sort")
    public ResponseEntity<?> getSortedServices(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<ServiceOfCarDto> sortedServices = serviceOfCarService.getSortedServices(sortBy, order, pageable);
        if (sortedServices.isEmpty()) {
            return new ResponseEntity<>("No services found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedServices, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredOrders(
            @RequestParam(required = false) Long carId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) BigDecimal cost,
            @RequestParam(required = false) ServiceOfCarStatus status,
            @PageableDefault Pageable pageable) {

        Page<ServiceOfCarDto> filteredServices = serviceOfCarService.getFilteredServices(
                carId, employeeId, startDate, endDate, description, cost, status, pageable);

        if (!filteredServices.hasContent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No services found."));
        }

        return ResponseEntity.ok(filteredServices);
    }
}
