package org.carrent.coursework.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.EmployeeDto;
import org.carrent.coursework.dto.OrderCreationDto;
import org.carrent.coursework.dto.OrderDto;
import org.carrent.coursework.enums.EmployeePosition;
import org.carrent.coursework.enums.OrderStatus;
import org.carrent.coursework.service.OrderService;
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
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @GetMapping("{id}")
    @Cacheable(value = "orders", key = "#id")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @GetMapping
    @Cacheable(value = "orders")
    public ResponseEntity<Page<OrderDto>> getAllOrders(
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
        return ResponseEntity.ok(orderService.getAll(pageable));
    }

    @PostMapping
    @CacheEvict(value = "orders", allEntries = true)
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderCreationDto orderCreationDto) {
        return new ResponseEntity<>(orderService.create(orderCreationDto), HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    @CacheEvict(value = "orders", allEntries = true)
    public ResponseEntity<OrderDto> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderDto orderDto
    ) {
        return ResponseEntity.ok(orderService.updateOrder(id, orderDto));
    }

    @GetMapping("/sort")
    public ResponseEntity<?> getSortedOrders(@RequestParam String sortBy, @RequestParam String order, @PageableDefault Pageable pageable) {
        Page<OrderDto> sortedOrders = orderService.getSortedOrders(sortBy, order, pageable);
        if (sortedOrders.isEmpty()) {
            return new ResponseEntity<>("No orders found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedOrders, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredOrders(
            @RequestParam(required = false) Long carId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) BigDecimal cost,
            @PageableDefault Pageable pageable) {

        Page<OrderDto> filteredOrders = orderService.getFilteredOrders(
                carId, customerId, employeeId, startDate, endDate, status, cost, pageable);

        if (!filteredOrders.hasContent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No orders found."));
        }

        return ResponseEntity.ok(filteredOrders);
    }

}
