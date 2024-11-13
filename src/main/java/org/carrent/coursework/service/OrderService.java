package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.OrderCreationDto;
import org.carrent.coursework.dto.OrderDto;
import org.carrent.coursework.entity.Order;
import org.carrent.coursework.exception.OrderNotFoundException;
import org.carrent.coursework.mapper.OrderMapper;
import org.carrent.coursework.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class OrderService{
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    public OrderDto getById(Long id){
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Order not found"));
        return orderMapper.toDto(order);
    }

    public List<OrderDto> getAll() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Transactional
    public OrderDto create(OrderCreationDto order){

        return orderMapper.toDto(orderRepository.save(orderMapper.toEntity(order)));
    }
}
