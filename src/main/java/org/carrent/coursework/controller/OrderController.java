package org.carrent.coursework.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.OrderCreationDto;
import org.carrent.coursework.dto.OrderDto;
import org.carrent.coursework.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
public class OrderController
{
    private final OrderService orderService;

    @GetMapping("{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id){
        return ResponseEntity.ok(orderService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders(){
        return ResponseEntity.ok(orderService.getAll());
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderCreationDto orderCreationDto){
        return new ResponseEntity<>(orderService.create(orderCreationDto), HttpStatus.CREATED);
    }
}
