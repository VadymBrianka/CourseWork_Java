package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.OrderDto;
import org.carrent.coursework.dto.ServiceOfCarCreationDto;
import org.carrent.coursework.dto.ServiceOfCarDto;
import org.carrent.coursework.entity.Car;
import org.carrent.coursework.entity.Order;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class ServiceOfCarService {
    private final ServiceOfCarRepository serviceOfCarRepository;
    private CarRepository carRepository;
    private EmployeeRepository employeeRepository;
    OrderRepository orderRepository;
    private final ServiceOfCarMapper serviceOfCarMapper;
    public ServiceOfCarDto getById(Long id){
        ServiceOfCar serviceOfCar = serviceOfCarRepository.findById(id).orElseThrow(() -> new ServiceOfCarNotFoundException("Service not found"));
        return serviceOfCarMapper.toDto(serviceOfCar);
    }

    public Page<ServiceOfCarDto> getAll(Pageable pageable) {
        return serviceOfCarRepository.findAll(pageable)
                .map(serviceOfCarMapper::toDto);
    }

    public ServiceOfCarDto updateServiceOfCar(Long id, ServiceOfCarDto serviceOfCarDto) {
        ServiceOfCar serviceOfCar = serviceOfCarRepository.findById(id)
                .orElseThrow(() -> new ServiceOfCarNotFoundException("Service of car with ID: " + id + " not found"));
        serviceOfCarMapper.partialUpdate(serviceOfCarDto, serviceOfCar);
        ServiceOfCar updatedServiceOfCar = serviceOfCarRepository.save(serviceOfCar);
        return serviceOfCarMapper.toDto(updatedServiceOfCar);
    }


    @Transactional
    public ServiceOfCarDto create(ServiceOfCarCreationDto serviceOfCarCreationDto) {
        // Перевірка існування автомобіля
        Car car = carRepository.findById(serviceOfCarCreationDto.carId())
                .orElseThrow(() -> new CarNotFoundException("Car not found with ID: " + serviceOfCarCreationDto.carId()));

        if(car.getStatus() == CarStatus.RENTED){
            throw new CarNotFoundException("Car  with ID: " + serviceOfCarCreationDto.carId() + " rented!");
        } else if (car.getStatus() == CarStatus.IN_SERVICE) {
            throw new CarNotFoundException("Car  with ID: " + serviceOfCarCreationDto.carId() + " is already in service!");
        }
        // Перевірка існування співробітника
        Employee employee = employeeRepository.findById(serviceOfCarCreationDto.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + serviceOfCarCreationDto.employeeId()));

        // Перевірка позиції співробітника
        if (employee.getPosition() == EmployeePosition.SALES_REPRESENTATIVE) {
            throw new InvalidEmployeePositionException("Employee with ID " + serviceOfCarCreationDto.employeeId() +
                    " cannot register a car for service because they are a Sales Representative.");
        }

        // Перевірка дублювання запису про сервіс
        if (serviceOfCarRepository.existsByCar_IdAndStartDateAndEndDateAndDescription(
                serviceOfCarCreationDto.carId(),
                serviceOfCarCreationDto.startDate(),
                serviceOfCarCreationDto.endDate(),
                serviceOfCarCreationDto.description())) {
            throw new ServiceOfCarAlreadyExistsException("Service with car_id " + serviceOfCarCreationDto.carId()
                    + ", start date " + serviceOfCarCreationDto.startDate()
                    + ", end date " + serviceOfCarCreationDto.endDate()
                    + " and description " + serviceOfCarCreationDto.description() + " already exists");
        }
        LocalDateTime today = LocalDateTime.now();

        if (orderRepository.findFirstByCar_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatusIn(
                car.getId(), today, today,
                List.of(OrderStatus.ACTIVE, OrderStatus.RESERVED)).isPresent()) {
            throw new CarNotAvailableException("Car is reserved during this period!");
        }

        if (serviceOfCarRepository.findFirstByCar_IdAndEndDateGreaterThanOrStartDateLessThan(
                car.getId(), today,today).isPresent()) {
            throw new CarNotAvailableException("Car is going to be in service during this period!");
        }

        // Створення об'єкта сервісу
        ServiceOfCar serviceOfCar = serviceOfCarMapper.toEntity(serviceOfCarCreationDto);
        serviceOfCar.setCar(car);
        serviceOfCar.setEmployee(employee);

        // Оновлення статусу автомобіля, якщо сервіс актуальний
        if ((serviceOfCar.getStartDate().isBefore(today) || serviceOfCar.getStartDate().isEqual(today)) &&
                (serviceOfCar.getEndDate().isAfter(today) || serviceOfCar.getEndDate().isEqual(today))) {
            car.setStatus(CarStatus.IN_SERVICE);
        }

        // Збереження об'єктів
        carRepository.save(car);
//        serviceOfCar.setStatus(ServiceOfCarStatus.RESERVED);
        ServiceOfCar savedServiceOfCar = serviceOfCarRepository.save(serviceOfCar);

        return serviceOfCarMapper.toDto(savedServiceOfCar);
    }

    @Transactional
    public void updateServiceOfCarStatuses() {
        System.out.println("Serviceeeeeeeeee");
        LocalDateTime now = LocalDateTime.now();
        List<ServiceOfCar> services = serviceOfCarRepository.findAll();

        for (ServiceOfCar service : services) {
            if ((service.getStatus() != ServiceOfCarStatus.COMPLETED) || (service.getStatus() != ServiceOfCarStatus.CANCELED)) {
                if (service.getEndDate().isBefore(now)) {
                    // Якщо поточний час перевищує час завершення обслуговування
                    service.setStatus(ServiceOfCarStatus.COMPLETED);
                } else if (service.getEndDate().isAfter(now)) {
                    // Якщо обслуговування ще не активне
                    service.setStatus(ServiceOfCarStatus.RESERVED);
                } else if (service.getStartDate().isBefore(now) && service.getEndDate().isAfter(now)) {
                    service.setStatus(ServiceOfCarStatus.ACTIVE);
                }
            }
        }
        serviceOfCarRepository.saveAll(services); // Масове збереження змін
    }

    @Transactional(readOnly = true)
    public Page<ServiceOfCarDto> getSortedServices(String sortBy, String order, Pageable pageable) {
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<ServiceOfCar> prdersPage = serviceOfCarRepository.findAll(sortedPageable);
        return prdersPage.map(serviceOfCarMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ServiceOfCarDto> getFilteredServices(Long carId,
                                                     Long employeeId,
                                                     LocalDateTime startDate,
                                                     LocalDateTime endDate,
                                                     String description,
                                                     BigDecimal cost,
                                                     ServiceOfCarStatus status,
                                                     Pageable pageable) {
        Specification<ServiceOfCar> specification = Specification.where(null);

        if (carId != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("car").get("id"), carId));
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
        if (description != null && !description.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
        }
        if (cost != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("cost"), cost));
        }
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }

        Page<ServiceOfCar> services = serviceOfCarRepository.findAll(specification, pageable);

        return services.map(service -> new ServiceOfCarDto(
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
    }


}