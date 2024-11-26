package org.carrent.coursework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.ServiceOfCarCreationDto;
import org.carrent.coursework.dto.ServiceOfCarDto;
import org.carrent.coursework.enums.ServiceOfCarStatus;
import org.carrent.coursework.service.ServiceOfCarService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Operation(
            summary = "Get service by ID",
            description = "Fetches service details based on provided ID",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully fetched service",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ServiceOfCarDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Service not found")
            }
    )
    @GetMapping("{id}")
    @Cacheable(value = "services", key = "#id")
    public ResponseEntity<ServiceOfCarDto> getServiceById(@PathVariable Long id) {
        return ResponseEntity.ok(serviceOfCarService.getById(id));
    }

    @Operation(
            summary = "Get all services",
            description = "Fetches all services with pagination",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully fetched services",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class)
                            )
                    )
            }
    )

    @GetMapping
    @Cacheable(value = "services")
    public ResponseEntity<Page<ServiceOfCarDto>> getAllServices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ServiceOfCarDto> services = serviceOfCarService.getAll(pageable);
        return ResponseEntity.ok(services);
    }

    @Operation(
            summary = "Create a new service",
            description = "Creates a new service entry in the system",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Details of the service to be created",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServiceOfCarCreationDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Successfully created service",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ServiceOfCarDto.class)
                            )
                    )
            }
    )
    @PostMapping
    @CacheEvict(value = "services", allEntries = true)
    public ResponseEntity<ServiceOfCarDto> createService(@Valid @RequestBody ServiceOfCarCreationDto serviceOfCarCreationDto) {
        return new ResponseEntity<>(serviceOfCarService.create(serviceOfCarCreationDto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update an existing service",
            description = "Updates the details of an existing service based on its ID",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Service update details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServiceOfCarDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated service",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ServiceOfCarDto.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "Service not found")
            }
    )
    @PutMapping("{id}")
    @CacheEvict(value = "services", allEntries = true)
    public ResponseEntity<ServiceOfCarDto> updateService(@PathVariable Long id, @Valid @RequestBody ServiceOfCarDto serviceOfCarDto) {
        return ResponseEntity.ok(serviceOfCarService.updateServiceOfCar(id, serviceOfCarDto));
    }

    @Operation(
            summary = "Get sorted services",
            description = "Fetches services sorted by specified field and order",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully fetched sorted services",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "No services found")
            }
    )
    @GetMapping("/sort")
    public ResponseEntity<?> getSortedServices(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<ServiceOfCarDto> sortedServices = serviceOfCarService.getSortedServices(sortBy, order, pageable);
        if (sortedServices.isEmpty()) {
            return new ResponseEntity<>("No services found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedServices, HttpStatus.OK);
    }

    @Operation(
            summary = "Filter services",
            description = "Fetches services that match the specified filters",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully fetched filtered services",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class)
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "No services found")
            }
    )
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

    @DeleteMapping("/{id}")
    @CacheEvict(value = "service", allEntries = true)
    @Operation(
            summary = "Delete a service by ID",
            description = "Deletes a service record from the database using the specified ID. Also clears the cache associated with the list of services.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Service successfully deleted",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Service with the specified ID not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "The ID of the service to delete",
                            required = true,
                            example = "567"
                    )
            }
    )
    public ResponseEntity<String> deleteService(@PathVariable Long id) {
        return ResponseEntity.ok(serviceOfCarService.deleteService(id));
    }


    @Operation(
            summary = "Get all available services",
            description = "Retrieves a paginated list of available services with optional sorting.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched list of available services",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
            }
    )
    @GetMapping("/available")
    @Cacheable(value = "services")
    public ResponseEntity<Page<ServiceOfCarDto>> getAllServicesAvailable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ServiceOfCarDto> services = serviceOfCarService.getAllAvailable(pageable);
        return ResponseEntity.ok(services);
    }
}
