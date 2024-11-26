package org.carrent.coursework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CustomerDto;
import org.carrent.coursework.dto.EmployeeCreationDto;
import org.carrent.coursework.dto.EmployeeDto;
import org.carrent.coursework.enums.EmployeePosition;
import org.carrent.coursework.service.EmployeeService;
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
@RequestMapping("/api/employees")
@AllArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @Operation(
            summary = "Get employee by ID",
            description = "Fetches employee details based on the provided ID",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched employee",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeDto.class))),
                    @ApiResponse(responseCode = "404", description = "Employee not found")
            }
    )
    @GetMapping("{id}")
    @Cacheable(value = "employees", key = "#id")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @Operation(
            summary = "Get all employees",
            description = "Fetches all employees with pagination and sorting options",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched employees",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid request parameters")
            }
    )
    @GetMapping
    @Cacheable(value = "employees")
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(employeeService.getAll(pageable));
    }

    @Operation(
            summary = "Create a new employee",
            description = "Creates a new employee and saves it to the database",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Employee successfully created",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeDto.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    @PostMapping
    @CacheEvict(value = "employees", allEntries = true)
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeCreationDto employeeCreationDto) {
        return new ResponseEntity<>(employeeService.create(employeeCreationDto), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Update an existing employee",
            description = "Updates details of an existing employee based on the provided ID",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Employee successfully updated",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmployeeDto.class))),
                    @ApiResponse(responseCode = "404", description = "Employee not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    @PutMapping("{id}")
    @CacheEvict(value = "employees", allEntries = true)
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDto employeeDto
    ) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employeeDto));
    }

    @Operation(
            summary = "Get sorted employees",
            description = "Fetches employees sorted by a specified field and order",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched sorted employees",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No employees found")
            }
    )
    @GetMapping("/sort")
    public ResponseEntity<?> getSortedEmployees(
            @RequestParam String sortBy,
            @RequestParam String order,
            @PageableDefault Pageable pageable
    ) {
        Page<EmployeeDto> sortedEmployees = employeeService.getSortedEmployees(sortBy, order, pageable);
        if (sortedEmployees.isEmpty()) {
            return new ResponseEntity<>("No employees found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedEmployees, HttpStatus.OK);
    }

    @Operation(
            summary = "Filter employees",
            description = "Fetches employees based on provided filter criteria",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched filtered employees",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No employees found")
            }
    )
    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredEmployees(
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String middleName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateOfBirth,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) EmployeePosition position,
            @PageableDefault Pageable pageable
    ) {
        Page<EmployeeDto> filteredEmployees = employeeService.getFilteredEmployees(
                lastName, firstName, middleName, dateOfBirth, email, phoneNumber, address, position, pageable);
        if (!filteredEmployees.hasContent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No employees found."));
        }
        return ResponseEntity.ok(filteredEmployees);
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "employees", allEntries = true)
    @Operation(
            summary = "Delete an employee by ID",
            description = "Deletes an employee from the database using the specified ID. Also clears the cache associated with the list of employees.",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Employee successfully deleted",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Employee with the specified ID not found",
                            content = @Content(mediaType = "application/json")
                    )
            },
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "The ID of the employee to delete",
                            required = true,
                            example = "789"
                    )
            }
    )
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.deleteEmployee(id));
    }



    @Operation(
            summary = "Get all available employees",
            description = "Retrieves a paginated list of available employees with optional sorting.",
            security = @SecurityRequirement(name = "BearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched list of available employees",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
            }
    )
    @GetMapping("/available")
    @Cacheable(value = "employees")
    public ResponseEntity<Page<EmployeeDto>> getAllEmployeesAvailable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EmployeeDto> employees = employeeService.getAllAvailable(pageable);
        return ResponseEntity.ok(employees);
    }
}
