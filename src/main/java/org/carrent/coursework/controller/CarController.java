package org.carrent.coursework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CarCreationDto;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.enums.CarStatus;
import org.carrent.coursework.service.CarService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    @Operation(
            summary = "Get car by ID",
            description = "Fetches car details based on the provided ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched car details",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CarDto.class))),
                    @ApiResponse(responseCode = "404", description = "Car not found")
            }
    )
    @GetMapping("{id}")
    @Cacheable(value = "cars", key = "#id")
    public ResponseEntity<CarDto> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(carService.getById(id));
    }

    @Operation(
            summary = "Get all cars with pagination",
            description = "Retrieves a paginated list of cars with optional sorting.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched list of cars",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
            }
    )
    @GetMapping
    @Cacheable(value = "cars")
    public ResponseEntity<Page<CarDto>> getAllCars(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CarDto> cars = carService.getAll(pageable);
        return ResponseEntity.ok(cars);
    }

    @GetMapping("/available")
    @Cacheable(value = "cars")
    public ResponseEntity<Page<CarDto>> getAllCarsAvailable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CarDto> cars = carService.getAllAvailable(pageable);
        return ResponseEntity.ok(cars);
    }

    @Operation(
            summary = "Create a new car",
            description = "Adds a new car to the system and clears cache.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Car created successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CarDto.class))),
            }
    )
    @PostMapping
    @CacheEvict(value = "cars", allEntries = true)
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CarCreationDto carCreationDto) {
        return new ResponseEntity<>(carService.create(carCreationDto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update car details",
            description = "Updates car details by ID and clears cache.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Car updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CarDto.class))),
                    @ApiResponse(responseCode = "404", description = "Car not found")
            }
    )
    @PutMapping("{id}")
    @CacheEvict(value = "cars", allEntries = true)
    public ResponseEntity<CarDto> updateCar(
            @PathVariable Long id,
            @Valid @RequestBody CarDto carDto
    ) {
        return ResponseEntity.ok(carService.updateCar(id, carDto));
    }

    @Operation(
            summary = "Get sorted cars",
            description = "Fetches a sorted list of cars based on specified criteria.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched sorted cars",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No cars found")
            }
    )
    @GetMapping("/sort")
    public ResponseEntity<?> getSortedCars(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<CarDto> sortedCars = carService.getSortedCars(sortBy, order, pageable);
        if (sortedCars.isEmpty()) {
            return new ResponseEntity<>("No cars found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedCars, HttpStatus.OK);
    }

    @Operation(
            summary = "Get filtered cars",
            description = "Retrieves a list of cars filtered by specific parameters.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched filtered cars",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No cars found")
            }
    )
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
        Page<CarDto> filteredCars = carService.getFilteredCars(brand, model, year, licensePlate, status, mileage, price, pageable);
        if (filteredCars.isEmpty()) {
            return new ResponseEntity<>("No cars found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(filteredCars, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "cars", allEntries = true)
    public ResponseEntity<String> deleteCar(@PathVariable Long id) {
        return ResponseEntity.ok(carService.deleteCar(id));
    }

}

