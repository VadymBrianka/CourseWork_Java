package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.OrderDto;
import org.carrent.coursework.dto.ServiceOfCarCreationDto;
import org.carrent.coursework.dto.ServiceOfCarDto;
import org.carrent.coursework.entity.Car;
import org.carrent.coursework.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.carrent.coursework.enums.CarStatus;
import org.carrent.coursework.enums.EmployeePosition;
import org.carrent.coursework.enums.OrderStatus;
import org.carrent.coursework.enums.ServiceOfCarStatus;
import org.carrent.coursework.exception.*;
import org.carrent.coursework.repository.CarRepository;
import org.carrent.coursework.repository.EmployeeRepository;
import org.carrent.coursework.entity.Employee;
import org.carrent.coursework.entity.ServiceOfCar;
import org.carrent.coursework.mapper.ServiceOfCarMapper;
import org.carrent.coursework.repository.OrderRepository;
import org.carrent.coursework.repository.ServiceOfCarRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class ServiceOfCarService {

    private static final Logger logger = LoggerFactory.getLogger(ServiceOfCarService.class);

    private final ServiceOfCarRepository serviceOfCarRepository;
    private final CarRepository carRepository;
    private final EmployeeRepository employeeRepository;
    private final OrderRepository orderRepository;
    private final ServiceOfCarMapper serviceOfCarMapper;

    public ServiceOfCarDto getById(Long id) {
        logger.info("Fetching service by ID: {}", id);
        ServiceOfCar serviceOfCar = serviceOfCarRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Service with ID: {} not found", id);
                    return new ServiceOfCarNotFoundException("Service not found");
                });
        logger.debug("Fetched service: {}", serviceOfCar);
        return serviceOfCarMapper.toDto(serviceOfCar);
    }

    public Page<ServiceOfCarDto> getAll(Pageable pageable) {
        logger.info("Fetching all services with pageable: {}", pageable);
        Page<ServiceOfCarDto> services = serviceOfCarRepository.findAll(pageable)
                .map(serviceOfCarMapper::toDto);
        logger.debug("Fetched {} services", services.getTotalElements());
        return services;
    }

    @Transactional
    public ServiceOfCarDto updateServiceOfCar(Long id, ServiceOfCarDto serviceOfCarDto) {
        logger.info("Updating service with ID: {}", id);
        ServiceOfCar serviceOfCar = serviceOfCarRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Service with ID: {} not found", id);
                    return new ServiceOfCarNotFoundException("Service of car with ID: " + id + " not found");
                });
        logger.debug("Existing service: {}", serviceOfCar);
        serviceOfCarMapper.partialUpdate(serviceOfCarDto, serviceOfCar);
        logger.debug("Updated service: {}", serviceOfCar);
        ServiceOfCar updatedServiceOfCar = serviceOfCarRepository.save(serviceOfCar);
        logger.info("Successfully updated service with ID: {}", updatedServiceOfCar.getId());
        return serviceOfCarMapper.toDto(updatedServiceOfCar);
    }

    @Transactional
    public ServiceOfCarDto create(ServiceOfCarCreationDto serviceOfCarCreationDto) {
        logger.info("Creating new service: {}", serviceOfCarCreationDto);

        Car car = carRepository.findById(serviceOfCarCreationDto.carId())
                .orElseThrow(() -> {
                    logger.error("Car with ID: {} not found", serviceOfCarCreationDto.carId());
                    return new CarNotFoundException("Car not found with ID: " + serviceOfCarCreationDto.carId());
                });
        logger.debug("Car found: {}", car);

        Employee employee = employeeRepository.findById(serviceOfCarCreationDto.employeeId())
                .orElseThrow(() -> {
                    logger.error("Employee with ID: {} not found", serviceOfCarCreationDto.employeeId());
                    return new EmployeeNotFoundException("Employee not found with ID: " + serviceOfCarCreationDto.employeeId());
                });
        logger.debug("Employee found: {}", employee);

        ServiceOfCar serviceOfCar = serviceOfCarMapper.toEntity(serviceOfCarCreationDto);
        serviceOfCar.setCar(car);
        serviceOfCar.setEmployee(employee);
        logger.debug("Mapped service entity: {}", serviceOfCar);

        ServiceOfCar savedServiceOfCar = serviceOfCarRepository.save(serviceOfCar);
        logger.info("Service successfully created with ID: {}", savedServiceOfCar.getId());

        return serviceOfCarMapper.toDto(savedServiceOfCar);
    }

    @Transactional
    public void updateServiceOfCarStatuses() {
        logger.info("Updating statuses for all services...");
        LocalDateTime now = LocalDateTime.now();
        List<ServiceOfCar> services = serviceOfCarRepository.findAll();
        logger.debug("Total services to process: {}", services.size());

        for (ServiceOfCar service : services) {
            logger.debug("Processing service with ID: {}", service.getId());
            if (service.getStatus() != ServiceOfCarStatus.COMPLETED && service.getStatus() != ServiceOfCarStatus.CANCELED) {
                if (service.getEndDate().isBefore(now)) {
                    service.setStatus(ServiceOfCarStatus.COMPLETED);
                } else if (service.getEndDate().isAfter(now)) {
                    service.setStatus(ServiceOfCarStatus.RESERVED);
                } else if (service.getStartDate().isBefore(now) && service.getEndDate().isAfter(now)) {
                    service.setStatus(ServiceOfCarStatus.ACTIVE);
                }
            }
            logger.debug("Updated service status to: {}", service.getStatus());
        }

        serviceOfCarRepository.saveAll(services);
        logger.info("Service statuses updated successfully.");
    }

    public Page<ServiceOfCarDto> getSortedServices(String sortBy, String order, Pageable pageable) {
        logger.info("Fetching sorted services by '{}' in '{}' order", sortBy, order);
        Page<ServiceOfCarDto> services = serviceOfCarRepository.findAll(pageable)
                .map(serviceOfCarMapper::toDto);
        logger.debug("Fetched {} sorted services", services.getTotalElements());
        return services;
    }

    public Page<ServiceOfCarDto> getFilteredServices(Long carId, Long employeeId, LocalDateTime startDate, LocalDateTime endDate,
                                                     String description, BigDecimal cost, ServiceOfCarStatus status, Pageable pageable) {
        logger.info("Filtering services with parameters - carId: {}, employeeId: {}, startDate: {}, endDate: {}, description: {}, cost: {}, status: {}, pageable: {}",
                carId, employeeId, startDate, endDate, description, cost, status, pageable);

        Specification<ServiceOfCar> specification = Specification.where(null);

        // Add filter specifications with logging
        if (carId != null) {
            logger.debug("Adding filter for carId: {}", carId);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("car").get("id"), carId));
        }
        if (employeeId != null) {
            logger.debug("Adding filter for employeeId: {}", employeeId);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("employee").get("id"), employeeId));
        }
        if (startDate != null) {
            logger.debug("Adding filter for startDate: {}", startDate);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), startDate));
        }
        if (endDate != null) {
            logger.debug("Adding filter for endDate: {}", endDate);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), endDate));
        }
        if (description != null && !description.isEmpty()) {
            logger.debug("Adding filter for description containing: {}", description);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
        }
        if (cost != null) {
            logger.debug("Adding filter for cost: {}", cost);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("cost"), cost));
        }
        if (status != null) {
            logger.debug("Adding filter for status: {}", status);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }

        Page<ServiceOfCar> services = serviceOfCarRepository.findAll(specification, pageable);
        logger.info("Found {} services matching the filters.", services.getTotalElements());

        Page<ServiceOfCarDto> result = services.map(service -> new ServiceOfCarDto(
                service.getId(),
                service.isDeleted(),
                service.getCreatedAt(),
                service.getUpdatedAt(),
                service.getCar().getId(),
                service.getEmployee().getId(),
                service.getStartDate(),
                service.getEndDate(),
                service.getDescription(),
                service.getCost(),
                service.getStatus()
        ));

        logger.info("Returning filtered services.");
        return result;
    }

    @Transactional
    public String deleteService(Long id) {
        logger.info("Deleting service with ID: {}", id);
        ServiceOfCar service = serviceOfCarRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Service with ID: {} not found", id);
                    return new ServiceOfCarNotFoundException("Service with ID: " + id + " not found");
                });
        service.setDeleted(true);
        serviceOfCarRepository.save(service);
        logger.info("Service with ID: {} marked as deleted.", id);
        return "Order with ID " + id + " has been deleted.";
    }

    public Page<ServiceOfCarDto> getAllAvailable(Pageable pageable) {
        logger.info("Fetching all available services...");
        Page<ServiceOfCar> services = serviceOfCarRepository.findAll(pageable);
        logger.debug("Total services fetched: {}", services.getTotalElements());
        Page<ServiceOfCarDto> availableServices = services.stream()
                .filter(service -> !service.isDeleted())
                .map(serviceOfCarMapper::toDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> new PageImpl<>(list, pageable, services.getTotalElements())));
        logger.info("Filtered {} available services.", availableServices.getTotalElements());
        return availableServices;
    }
}