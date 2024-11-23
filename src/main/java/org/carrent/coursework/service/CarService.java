package org.carrent.coursework.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CarCreationDto;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.entity.Car;
import org.carrent.coursework.entity.Order;
import org.carrent.coursework.entity.ServiceOfCar;
import org.carrent.coursework.enums.CarStatus;
import org.carrent.coursework.exception.CarAlreadyExistsException;
import org.carrent.coursework.exception.CarNotFoundException;
import org.carrent.coursework.repository.CarRepository;
import org.carrent.coursework.repository.OrderRepository;
import org.carrent.coursework.mapper.CarMapper;
import org.carrent.coursework.repository.ServiceOfCarRepository;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class CarService {

    private static final Logger logger = LoggerFactory.getLogger(CarService.class);
    private final CarRepository carRepository;
    private final OrderRepository orderRepository;
    private final CarMapper carMapper;
    private final ServiceOfCarRepository serviceOfCarRepository;

    public CarDto getById(Long id) {
        logger.info("Fetching car by ID: {}", id);
        Car car = carRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Car not found with ID: {}", id);
                    return new CarNotFoundException("Car not found");
                });
        logger.info("Car with ID: {} successfully fetched", id);
        return carMapper.toDto(car);
    }

    public Page<CarDto> getAll(Pageable pageable) {
        logger.info("Fetching all cars with pagination: {}", pageable);
        return carRepository.findAll(pageable)
                .map(carMapper::toDto);
    }

    public Page<CarDto> getAllAvailable(Pageable pageable) {
        logger.info("Fetching all available cars with pagination: {}", pageable);
        Page<Car> cars = carRepository.findAll(pageable);
        logger.info("Fetched {} cars for filtering available ones", cars.getTotalElements());
        return cars.stream()
                .filter(car -> !car.isDeleted())
                .map(carMapper::toDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        list -> new PageImpl<>(list, pageable, cars.getTotalElements())));
    }

    @Transactional
    public CarDto updateCar(Long id, CarDto carDto) {
        logger.info("Updating car with ID: {}", id);
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException("Car with ID: " + id + " not found"));
        logger.info("Car found: {}", car);
        carMapper.partialUpdate(carDto, car);
        Car updatedCar = carRepository.save(car);
        logger.info("Car with ID: {} successfully updated", id);
        return carMapper.toDto(updatedCar);
    }

    @Transactional
    public String deleteCar(Long id) {
        logger.info("Deleting car with ID: {}", id);
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException("Car with ID: " + id + " not found"));
        car.setDeleted(true);
        carRepository.save(car);
        logger.info("Car with ID: {} marked as deleted", id);
        return "Car with ID " + id + " has been deleted.";
    }

    @Transactional
    public CarDto create(CarCreationDto carCreationDto) {
        logger.info("Creating car with license plate: {}", carCreationDto.licensePlate());
        if (carRepository.existsByLicensePlate(carCreationDto.licensePlate())) {
            logger.warn("Car with license plate {} already exists", carCreationDto.licensePlate());
            throw new CarAlreadyExistsException("Car with license plate " + carCreationDto.licensePlate() + " already exists");
        }

        Car car = carMapper.toEntity(carCreationDto);
        car.setStatus(CarStatus.AVAILABLE);
        Car savedCar = carRepository.save(car);
        logger.info("Car with license plate: {} created successfully", carCreationDto.licensePlate());
        return carMapper.toDto(savedCar);
    }

    public Page<CarDto> getSortedCars(String sortBy, String order, Pageable pageable) {
        logger.info("Fetching sorted cars by {} in {} order", sortBy, order);
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Car> carsPage = carRepository.findAll(sortedPageable);
        logger.info("Fetched {} cars", carsPage.getTotalElements());
        return carsPage.map(carMapper::toDto);
    }

    public Page<CarDto> getFilteredCars(String brand, String model, Integer year, String licensePlate,
                                        CarStatus status, Long mileage, BigDecimal price, Pageable pageable) {
        logger.info("Fetching filtered cars with parameters: brand={}, model={}, year={}, licensePlate={}, status={}, mileage={}, price={}",
                brand, model, year, licensePlate, status, mileage, price);

        Specification<Car> specification = Specification.where(null);

        if (brand != null && !brand.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), "%" + brand.toLowerCase() + "%"));
        }
        if (model != null && !model.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("model")), "%" + model.toLowerCase() + "%"));
        }
        if (licensePlate != null && !licensePlate.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("licensePlate")), "%" + licensePlate.toLowerCase() + "%"));
        }
        if (year != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("year"), year));
        }
        if (mileage != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("mileage"), mileage));
        }
        if (price != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("price"), price));
        }
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }

        Page<Car> cars = carRepository.findAll(specification, pageable);
        logger.info("Fetched {} cars with applied filters", cars.getTotalElements());

        return cars.map(carMapper::toDto);
    }

    @Transactional
    public void updateCarStatuses() {
        logger.info("Updating car statuses");
        LocalDateTime now = LocalDateTime.now();
        List<Car> cars = carRepository.findAll();

        if (cars.isEmpty()) {
            logger.warn("No cars found to update statuses");
            return;
        }

        for (Car car : cars) {
            logger.info("Processing car with ID: {}", car.getId());
            if (car.getStatus() == CarStatus.RENTED) {
                Optional<Order> activeOrder = orderRepository.findFirstByCar_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        car.getId(), now, now);
                if (activeOrder.isEmpty()) {
                    car.setStatus(CarStatus.AVAILABLE);
                    logger.info("Car ID: {} status changed to AVAILABLE (no active orders)", car.getId());
                }
            } else if (car.getStatus() == CarStatus.IN_SERVICE) {
                Optional<ServiceOfCar> activeService = serviceOfCarRepository.findFirstByCar_IdAndEndDateGreaterThanEqual(
                        car.getId(), now);
                if (activeService.isEmpty()) {
                    car.setStatus(CarStatus.AVAILABLE);
                    logger.info("Car ID: {} status changed to AVAILABLE (service completed)", car.getId());
                }
            }
        }

        carRepository.saveAll(cars);
        logger.info("Car statuses updated successfully");
    }
}