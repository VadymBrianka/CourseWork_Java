package org.carrent.coursework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(
            summary = "Get order by ID",
            description = "Fetches order details based on provided ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched order",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDto.class))),
                    @ApiResponse(responseCode = "404", description = "Order not found")
            }
    )
    @GetMapping("{id}")
    @Cacheable(value = "orders", key = "#id")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @Operation(
            summary = "Get all orders",
            description = "Fetches a paginated list of orders with optional sorting.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched orders",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No orders found")
            }
    )
    @GetMapping
    @Cacheable(value = "orders")
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(orderService.getAll(pageable));
    }

    @Operation(
            summary = "Create a new order",
            description = "Creates a new order with the provided details.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order creation details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderCreationDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Successfully created order",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = OrderDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid order data")
            }
    )
    @PostMapping
    @CacheEvict(value = "orders", allEntries = true)
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderCreationDto orderCreationDto) {
        return new ResponseEntity<>(orderService.create(orderCreationDto), HttpStatus.CREATED);
    }


    @Operation(
            summary = "Update an existing order",
            description = "Updates the details of an existing order based on its ID.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Order update details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrderDto.class)
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully updated order",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = OrderDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Order not found",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PutMapping("{id}")
    @CacheEvict(value = "orders", allEntries = true)
    public ResponseEntity<OrderDto> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody OrderDto orderDto
    ) {
        return ResponseEntity.ok(orderService.updateOrder(id, orderDto));
    }


    @Operation(
            summary = "Get sorted orders",
            description = "Fetches a list of orders sorted by a specified field and order.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched sorted orders",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No orders found")
            }
    )
    @GetMapping("/sort")
    public ResponseEntity<?> getSortedOrders(
            @RequestParam String sortBy,
            @RequestParam String order,
            @PageableDefault Pageable pageable
    ) {
        Page<OrderDto> sortedOrders = orderService.getSortedOrders(sortBy, order, pageable);
        if (sortedOrders.isEmpty()) {
            return new ResponseEntity<>("No orders found.", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(sortedOrders, HttpStatus.OK);
    }

    @Operation(
            summary = "Filter orders",
            description = "Fetches orders based on various filter criteria.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully fetched filtered orders",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = Page.class))),
                    @ApiResponse(responseCode = "404", description = "No orders found")
            }
    )
    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredOrders(
            @RequestParam(required = false) Long carId,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime endDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime startDate,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) BigDecimal cost,
            @PageableDefault Pageable pageable
    ) {
        Page<OrderDto> filteredOrders = orderService.getFilteredOrders(
                carId, customerId, employeeId, startDate, endDate, status, cost, pageable);

        if (!filteredOrders.hasContent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No orders found."));
        }

        return ResponseEntity.ok(filteredOrders);
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = "order", allEntries = true)
    public ResponseEntity<String> deleteOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.deleteOrder(id));
    }

    @GetMapping("/available")
    @Cacheable(value = "orders")
    public ResponseEntity<Page<OrderDto>> getAllOrdersAvailable(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Sort sort = order.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<OrderDto> orders = orderService.getAllAvailable(pageable);
        return ResponseEntity.ok(orders);
    }
}
