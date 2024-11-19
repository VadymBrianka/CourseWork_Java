package org.carrent.coursework.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.EmployeeCreationDto;
import org.carrent.coursework.dto.EmployeeDto;
import org.carrent.coursework.enums.EmployeePosition;
import org.carrent.coursework.service.EmployeeService;
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

    @GetMapping("{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,        // Номер сторінки
            @RequestParam(defaultValue = "10") int size,       // Розмір сторінки
            @RequestParam(defaultValue = "id") String sortBy,  // Поле сортування
            @RequestParam(defaultValue = "asc") String order   // Порядок сортування
    ) {
        // Налаштовуємо пагінацію та сортування
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Повертаємо сторінковий результат
        return ResponseEntity.ok(employeeService.getAll(pageable));
    }

    @PostMapping
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeCreationDto employeeCreationDto) {
        return new ResponseEntity<>(employeeService.create(employeeCreationDto), HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeDto employeeDto
    ) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, employeeDto));
    }

    @GetMapping("/sort")
    public ResponseEntity<?> getSortedEmployees(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<EmployeeDto> sortedEmployees = employeeService.getSortedEmployees(sortBy, order, pageable);
        if (sortedEmployees.isEmpty()) {
            return new ResponseEntity<>("No employees found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedEmployees, HttpStatus.OK);
    }

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
            @PageableDefault Pageable pageable) {

        Page<EmployeeDto> filteredEmployees = employeeService.getFilteredEmployees(
                lastName, firstName, middleName, dateOfBirth, email, phoneNumber, address, position, pageable);

        if (!filteredEmployees.hasContent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No employees found."));
        }

        return ResponseEntity.ok(filteredEmployees);
    }

}
