package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.OrderCreationDto;
import org.carrent.coursework.dto.OrderDto;
import org.carrent.coursework.entity.*;
import org.carrent.coursework.enums.CarStatus;
import org.carrent.coursework.enums.EmployeePosition;
import org.carrent.coursework.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.carrent.coursework.exception.*;
import org.carrent.coursework.mapper.OrderMapper;
import org.carrent.coursework.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private CarRepository carRepository;
    private EmployeeRepository employeeRepository;
    private CustomerRepository customerRepository;
    private final OrderMapper orderMapper;
    private final ServiceOfCarRepository serviceOfCarRepository;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public OrderDto getById(Long id) {
        logger.info("Fetching order with ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", id);
                    return new OrderNotFoundException("Order not found");
                });
        logger.debug("Order found: {}", order);
        return orderMapper.toDto(order);
    }

    public Page<OrderDto> getAll(Pageable pageable) {
        logger.info("Fetching all orders with pageable: {}", pageable);
        Page<OrderDto> result = orderRepository.findAll(pageable)
                .map(orderMapper::toDto);
        logger.debug("Total orders fetched: {}", result.getTotalElements());
        return result;
    }


    public Page<OrderDto> getAllAvailable(Pageable pageable) {
        logger.info("Fetching all available orders with pageable: {}", pageable);
        Page<Order> orders = orderRepository.findAll(pageable);
        Page<OrderDto> availableOrders = orders.stream()
                .filter(order -> !order.isDeleted())
                .map(orderMapper::toDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(), list ->
                        new PageImpl<>(list, pageable, orders.getTotalElements())));
        logger.debug("Total available orders fetched: {}", availableOrders.getTotalElements());
        return availableOrders;
    }


    @Transactional
    public OrderDto updateOrder(Long id, OrderDto orderDto) {
        Logger logger = LoggerFactory.getLogger(getClass());

        logger.info("Called updateOrder with id: {}, orderDto: {}", id, orderDto);

        // Fetch the existing order
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Order with ID: {} not found", id);
                    return new OrderNotFoundException("Order with ID: " + id + " not found");
                });

        logger.debug("Fetched existing order: {}", order);

        // Update the order
        orderMapper.partialUpdate(orderDto, order);
        logger.debug("Order after applying updates: {}", order);

        // Save the updated order
        Order updatedOrder = orderRepository.save(order);
        logger.info("Order with ID: {} successfully updated", updatedOrder.getId());

        // Map the updated order to DTO
        OrderDto updatedOrderDto = orderMapper.toDto(updatedOrder);
        logger.debug("Mapped updated order to DTO: {}", updatedOrderDto);

        return updatedOrderDto;
    }



    @Transactional
    public OrderDto create(OrderCreationDto orderDto) {
        logger.info("Creating new order: {}", orderDto);

        // Validate car existence
        Car car = carRepository.findById(orderDto.carId())
                .orElseThrow(() -> {
                    logger.error("Car not found with ID: {}", orderDto.carId());
                    return new CarNotFoundException("Car not found with ID: " + orderDto.carId());
                });

        // Validate employee existence
        Employee employee = employeeRepository.findById(orderDto.employeeId())
                .orElseThrow(() -> {
                    logger.error("Employee not found with ID: {}", orderDto.employeeId());
                    return new EmployeeNotFoundException("Employee not found with ID: " + orderDto.employeeId());
                });

        // Validate employee position
        if (employee.getPosition() == EmployeePosition.TECHNICIAN) {
            logger.error("Technicians cannot create orders. Employee ID: {}", orderDto.employeeId());
            throw new EmployeePositionNotAllowedException("Technicians cannot create orders");
        }

        // Validate customer existence
        Customer customer = customerRepository.findById(orderDto.customerId())
                .orElseThrow(() -> {
                    logger.error("Customer not found with ID: {}", orderDto.customerId());
                    return new CustomerNotFoundException("Customer not found with ID: " + orderDto.customerId());
                });

        // Additional validations
        logger.debug("Validating order constraints for car ID: {}", orderDto.carId());
        LocalDateTime today = LocalDateTime.now();
        if (orderRepository.findFirstByCarAndDateRangeAndStatuses(
                car.getId(), today, today,
                List.of(OrderStatus.ACTIVE, OrderStatus.RESERVED)).isPresent()) {
            logger.error("Car is reserved during this period: Car ID: {}", orderDto.carId());
            throw new CarNotAvailableException("Car is reserved during this period!");
        }

        logger.debug("Mapping DTO to entity for order creation");
        Order order = orderMapper.toEntity(orderDto);
        order.setCar(car);
        order.setEmployee(employee);
        order.setCustomer(customer);

        logger.debug("Saving order to the database");
        Order savedOrder = orderRepository.save(order);

        // Update car status
        if ((order.getStartDate().isBefore(today) || order.getStartDate().isEqual(today)) &&
                (order.getEndDate().isAfter(today) || order.getEndDate().isEqual(today))) {
            car.setStatus(CarStatus.RENTED);
            carRepository.save(car);
            logger.info("Car status updated to RENTED for Car ID: {}", car.getId());
        }

        order.setStatus(OrderStatus.RESERVED);
        logger.info("Order created successfully with ID: {}", savedOrder.getId());
        return orderMapper.toDto(savedOrder);
    }


    @Transactional
    public void updateOrderStatuses() {
        logger.info("Updating order statuses");
        LocalDateTime now = LocalDateTime.now();
        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {
            logger.debug("Processing order ID: {}", order.getId());
            if ((order.getStatus() != OrderStatus.CANCELED) || (order.getStatus() != OrderStatus.COMPLETED)) {
                if (order.getStartDate().isAfter(now)) {
                    order.setStatus(OrderStatus.RESERVED);
                } else if (order.getStartDate().isBefore(now) && order.getEndDate().isAfter(now)) {
                    order.setStatus(OrderStatus.ACTIVE);
                } else if (order.getEndDate().isBefore(now)) {
                    order.setStatus(OrderStatus.COMPLETED);
                }
                logger.debug("Updated status for order ID: {}", order.getId());
            }
        }
        orderRepository.saveAll(orders);
        logger.info("Order statuses updated successfully");
    }


    public Page<OrderDto> getSortedOrders(String sortBy, String order, Pageable pageable) {
        Logger logger = LoggerFactory.getLogger(getClass());

        logger.info("Called getSortedOrders with sortBy: {}, order: {}, pageable: {}", sortBy, order, pageable);

        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        logger.debug("Constructed sorted Pageable: {}", sortedPageable);

        Page<Order> ordersPage = orderRepository.findAll(sortedPageable);

        logger.info("Found {} orders", ordersPage.getTotalElements());

        return ordersPage.map(orderMapper::toDto);
    }

    public Page<OrderDto> getFilteredOrders(Long carId,
                                            Long customerId,
                                            Long employeeId,
                                            LocalDateTime startDate,
                                            LocalDateTime endDate,
                                            OrderStatus status,
                                            BigDecimal cost,
                                            Pageable pageable) {
        Logger logger = LoggerFactory.getLogger(getClass());

        logger.info("Called getFilteredOrders with carId: {}, customerId: {}, employeeId: {}, startDate: {}, endDate: {}, status: {}, cost: {}, pageable: {}",
                carId, customerId, employeeId, startDate, endDate, status, cost, pageable);

        Specification<Order> specification = Specification.where(null);

        if (carId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("car").get("id"), carId));
            logger.debug("Added filter by carId: {}", carId);
        }
        if (customerId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("customer").get("id"), customerId));
            logger.debug("Added filter by customerId: {}", customerId);
        }
        if (employeeId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("employee").get("id"), employeeId));
            logger.debug("Added filter by employeeId: {}", employeeId);
        }
        if (startDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate));
            logger.debug("Added filter by startDate: {}", startDate);
        }
        if (endDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate));
            logger.debug("Added filter by endDate: {}", endDate);
        }
        if (cost != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("cost"), cost));
            logger.debug("Added filter by cost: {}", cost);
        }
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
            logger.debug("Added filter by status: {}", status);
        }

        Page<Order> orders = orderRepository.findAll(specification, pageable);

        logger.info("Found {} orders matching filters", orders.getTotalElements());

        return orders.map(order -> {
            logger.debug("Mapping Order to OrderDto: {}", order);
            return new OrderDto(
                    order.getId(),
                    order.isDeleted(),
                    order.getCreatedAt(),
                    order.getUpdatedAt(),
                    order.getCar().getId(),
                    order.getCustomer().getId(),
                    order.getEmployee().getId(),
                    order.getStartDate(),
                    order.getEndDate(),
                    order.getStatus(),
                    order.getCost()
            );
        });
    }

    @Transactional
    public String deleteOrder(Long id) {
        logger.info("Deleting order with ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID: " + id + " not found"));
        order.setDeleted(true);
        orderRepository.save(order);
        logger.info("Order with ID: {} marked as deleted.", id);
        return "Order with ID " + id + " has been deleted.";
    }

}