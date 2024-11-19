package org.carrent.coursework.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CarCreationDto;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.enums.CarStatus;
import org.carrent.coursework.service.CarService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
@AllArgsConstructor
public class CarController {
    private final CarService carService;

    @GetMapping("{id}")
    public ResponseEntity<CarDto> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(carService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<CarDto>> getAllCars(
            @RequestParam(defaultValue = "0") int page,        // Номер сторінки
            @RequestParam(defaultValue = "10") int size,       // Розмір сторінки
            @RequestParam(defaultValue = "id") String sortBy,  // Поле сортування
            @RequestParam(defaultValue = "asc") String order   // Порядок сортування
    ) {
        // Налаштовуємо сортування
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Отримуємо результат з пагінацією
        Page<CarDto> cars = carService.getAll(pageable);

        // Повертаємо відповідь
        return ResponseEntity.ok(cars);
    }

    @PostMapping
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CarCreationDto carCreationDto) {
        return new ResponseEntity<>(carService.create(carCreationDto), HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<CarDto> updateCar(
            @PathVariable Long id,
            @Valid @RequestBody CarDto carDto
    ) {
        return ResponseEntity.ok(carService.updateCar(id, carDto));
    }

    @GetMapping("/sort")
    public ResponseEntity<?> getSortedCars(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<CarDto> sortedCars = carService.getSortedCars(sortBy, order, pageable);
        if (sortedCars.isEmpty()) {
            return new ResponseEntity<>("No cars found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedCars, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredCars(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String licensePlate,
            @RequestParam(required = false) CarStatus status,
            @RequestParam(required = false) Long mileage,
            @RequestParam(required = false) BigDecimal price,
            @PageableDefault Pageable pageable
    ) {
        Page<CarDto> filteredCars = carService.getFilteredCars(brand, model, year, licensePlate,status,mileage,price, pageable);
        if (filteredCars.isEmpty()) {
            return new ResponseEntity<>("No cars found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredCars, HttpStatus.OK);
    }

}
