package org.carrent.coursework.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.dto.CustomerCreationDto;
import org.carrent.coursework.dto.CustomerDto;
import org.carrent.coursework.enums.CarStatus;
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

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@AllArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping("{id}")
    @Cacheable(value = "customers", key = "#id")
    public ResponseEntity<CustomerDto> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping
    @Cacheable(value = "customers")
    public ResponseEntity<Page<CustomerDto>> getAllCustomers(
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
        return ResponseEntity.ok(customerService.getAll(pageable));
    }

    @PostMapping
    @CacheEvict(value = "customers", allEntries = true)
    public ResponseEntity<CustomerDto> createCustomer(@Valid @RequestBody CustomerCreationDto customerCreationDto) {
        return new ResponseEntity<>(customerService.create(customerCreationDto), HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    @CacheEvict(value = "customers", allEntries = true)
    public ResponseEntity<CustomerDto> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerDto customerDto
    ) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customerDto));
    }

    @GetMapping("/sort")
    public ResponseEntity<?> getSortedCustomers(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<CustomerDto> sortedCustomers = customerService.getSortedCustomers(sortBy, order, pageable);
        if (sortedCustomers.isEmpty()) {
            return new ResponseEntity<>("No customers found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedCustomers, HttpStatus.OK);
    }

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


}
