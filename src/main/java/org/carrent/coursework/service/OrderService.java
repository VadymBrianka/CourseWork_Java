package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.dto.OrderCreationDto;
import org.carrent.coursework.dto.OrderDto;
import org.carrent.coursework.entity.*;
import org.carrent.coursework.enums.CarStatus;
import org.carrent.coursework.enums.EmployeePosition;
import org.carrent.coursework.enums.OrderStatus;
import org.carrent.coursework.enums.ServiceOfCarStatus;
import org.carrent.coursework.exception.*;
import org.carrent.coursework.mapper.OrderMapper;
import org.carrent.coursework.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    public OrderDto getById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        return orderMapper.toDto(order);
    }

    public Page<OrderDto> getAll(Pageable pageable) {
        return orderRepository.findAll(pageable) // Використовуємо пагінацію
                .map(orderMapper::toDto); // Перетворюємо Order у OrderDto
    }

    public OrderDto updateOrder(Long id, OrderDto orderDto) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID: " + id + " not found"));
        orderMapper.partialUpdate(orderDto, order);
        Order updatedOrder = orderRepository.save(order);
        return orderMapper.toDto(updatedOrder);
    }


    @Transactional
    public OrderDto create(OrderCreationDto orderDto) {
        // Перевірка існування автомобіля
        Car car = carRepository.findById(orderDto.carId())
                .orElseThrow(() -> new CarNotFoundException("Car not found with ID: " + orderDto.carId()));

        // Перевірка існування співробітника
        Employee employee = employeeRepository.findById(orderDto.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + orderDto.employeeId()));

        // Перевірка позиції співробітника
        if (employee.getPosition() == EmployeePosition.TECHNICIAN) {
            throw new EmployeePositionNotAllowedException("Technicians cannot create orders");
        }

        // Перевірка існування клієнта
        Customer customer = customerRepository.findById(orderDto.customerId())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + orderDto.customerId()));

        LocalDateTime today = LocalDateTime.now();

        // Перевірка перетину ордерів
//        if (orderRepository.findFirstByCar_IdAndEndDateGreaterThanOrStartDateLessThanAndStatusIsNotLike(
//                car.getId(), today, today, OrderStatus.CANCELED).isPresent()) {
//            throw new CarNotAvailableException("Car is reserved during this period!");
//        }

        if (orderRepository.findFirstByCarAndDateRangeAndStatuses(
                car.getId(), today, today,
                List.of(OrderStatus.ACTIVE, OrderStatus.RESERVED)).isPresent()) {
            throw new CarNotAvailableException("Car is reserved during this period!");
        }

        if (serviceOfCarRepository.findFirstByCarIdAndDateRange(
                car.getId(), today, today).isPresent()) {
            throw new CarNotAvailableException("Car is going to be in service during this period!");
        }
        if (orderRepository.existsByCar_IdAndStartDateAndEndDate(
                orderDto.carId(),
                orderDto.startDate(),
                orderDto.endDate())) {
            throw new OrderAlreadyExistsException("Order with car_id " + orderDto.carId()
                    + ", start date " + orderDto.startDate()
                    + ", end date " + orderDto.endDate()
                    + " already exists");
        }

        // Створення замовлення
        Order order = orderMapper.toEntity(orderDto);
        order.setCar(car);
        order.setEmployee(employee);
        order.setCustomer(customer);

        // Збереження замовлення
        Order savedOrder = orderRepository.save(order);

        // Оновлення статусу автомобіля
        if ((order.getStartDate().isBefore(today) || order.getStartDate().isEqual(today)) &&
                (order.getEndDate().isAfter(today) || order.getEndDate().isEqual(today))) {
            car.setStatus(CarStatus.RENTED);
            carRepository.save(car); // Збереження оновленого статусу автомобіля
        }
        order.setStatus(OrderStatus.RESERVED);
        return orderMapper.toDto(savedOrder);
    }


    @Transactional
    public void updateOrderStatuses() {
        System.out.println("Orderrrrrrrrrr");
        LocalDateTime now = LocalDateTime.now();
        List<Order> orders = orderRepository.findAll();

        for (Order order : orders) {
            if ((order.getStatus() != OrderStatus.CANCELED) || (order.getStatus() != OrderStatus.COMPLETED)) {
                if (order.getStartDate().isAfter(now)) {
                    // Ордер ще не активний
                    order.setStatus(OrderStatus.RESERVED);
                } else if (order.getStartDate().isBefore(now) && order.getEndDate().isAfter(now)) {
                    // Ордер активний
                    order.setStatus(OrderStatus.ACTIVE);
                } else if (order.getEndDate().isBefore(now)) {
                    // Ордер завершений
                    order.setStatus(OrderStatus.COMPLETED);
                }
            }
        }
        orderRepository.saveAll(orders); // Масове збереження
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getSortedOrders(String sortBy, String order, Pageable pageable) {
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Order> prdersPage = orderRepository.findAll(sortedPageable);
        return prdersPage.map(orderMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getFilteredOrders(Long carId,
                                            Long customerId,
                                            Long employeeId,
                                            LocalDateTime startDate,
                                            LocalDateTime endDate,
                                            OrderStatus status,
                                            BigDecimal cost,
                                            Pageable pageable) {
        Specification<Order> specification = Specification.where(null);

        if (carId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("car").get("id"), carId));
        }
        if (customerId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("customer").get("id"), customerId));
        }
        if (employeeId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("employee").get("id"), employeeId));
        }
        if (startDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate));
        }
        if (endDate != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate));
        }
        if (cost != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("cost"), cost));
        }
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }

        Page<Order> orders = orderRepository.findAll(specification, pageable);

        return orders.map(order -> new OrderDto(
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
        ));
    }


}