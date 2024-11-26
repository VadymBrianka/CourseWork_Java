package org.carrent.coursework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CustomerCreationDto;
import org.carrent.coursework.dto.CustomerDto;
import org.carrent.coursework.service.CustomerService;
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

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@AllArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @Operation(
            summary = "Get customer by ID",
            description = "Fetches customer details based on the provided ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched customer details",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CustomerDto.class))),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    @GetMapping("{id}")
    @Cacheable(value = "customers", key = "#id")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @Operation(
            summary = "Get all customers with pagination",
            description = "Retrieves a paginated list of customers with optional sorting.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched list of customers",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
            }
    )
    @GetMapping
    @Cacheable(value = "customers")
    public ResponseEntity<Page<CustomerDto>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(customerService.getAll(pageable));
    }

    @Operation(
            summary = "Create a new customer",
            description = "Adds a new customer to the system and clears cache.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Customer created successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CustomerDto.class))),
            }
    )
    @PostMapping
    @CacheEvict(value = "customers", allEntries = true)
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerCreationDto customerCreationDto) {
        return new ResponseEntity<>(customerService.create(customerCreationDto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update customer details",
            description = "Updates customer details by ID and clears cache.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Customer updated successfully",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CustomerDto.class))),
                    @ApiResponse(responseCode = "404", description = "Customer not found")
            }
    )
    @PutMapping("{id}")
    @CacheEvict(value = "customers", allEntries = true)
    public ResponseEntity<CustomerDto> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDto customerDto
    ) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customerDto));
    }

    @Operation(
            summary = "Get sorted customers",
            description = "Fetches a sorted list of customers based on specified criteria.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched sorted customers",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No customers found")
            }
    )
    @GetMapping("/sort")
    public ResponseEntity<?> getSortedCustomers(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<CustomerDto> sortedCustomers = customerService.getSortedCustomers(sortBy, order, pageable);
        if (sortedCustomers.isEmpty()) {
            return new ResponseEntity<>("No customers found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedCustomers, HttpStatus.OK);
    }

    @Operation(
            summary = "Get filtered customers",
            description = "Retrieves a list of customers filtered by specific parameters.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched filtered customers",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No customers found")
            }
    )
    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredCustomers(
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String middleName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateOfBirth,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String licenseNumber,
            @PageableDefault Pageable pageable) {
        Page<CustomerDto> filteredCustomers = customerService.getFilteredCustomers(
                lastName, firstName, middleName, dateOfBirth, email, phoneNumber, address, licenseNumber, pageable);
        if (!filteredCustomers.hasContent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No customers found."));
        }
        return ResponseEntity.ok(filteredCustomers);
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "customers", allEntries = true)
    @Operation(
            summary = "Delete a customer by ID",
            description = "Deletes a customer from the database using the specified ID. Clears the cache associated with the list of customers.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Customer successfully deleted",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Customer with the specified ID not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "The ID of the customer to delete",
                            required = true,
                            example = "456"
                    )
            }
    )
    public ResponseEntity<String> deleteCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.deleteCustomer(id));
    }



    @Operation(
            summary = "Get all available customers",
            description = "Retrieves a paginated list of available customers with optional sorting.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched list of available customers",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
            }
    )
    @GetMapping("/available")
    @Cacheable(value = "customers")
    public ResponseEntity<Page<CustomerDto>> getAllCustomersAvailable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CustomerDto> customers = customerService.getAllAvailable(pageable);
        return ResponseEntity.ok(customers);
    }

}
