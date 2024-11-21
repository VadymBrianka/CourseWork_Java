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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class  CarService {

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
        return carRepository.findAll(pageable) // Використовуємо пагінацію
                .map(carMapper::toDto); // Перетворюємо кожен Car в CarDto
    }

    public CarDto updateCar(Long id, CarDto carDto) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new CarNotFoundException("Car with ID: " + id + " not found"));
        carMapper.partialUpdate(carDto, car);
        return carMapper.toDto(carRepository.save(car));
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


    @Transactional(readOnly = true)
    public Page<CarDto> getSortedCars(String sortBy, String order, Pageable pageable) {
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Car> carsPage = carRepository.findAll(sortedPageable);
        return carsPage.map(carMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CarDto> getFilteredCars(String brand,
                                        String model,
                                        Integer year,
                                        String licensePlate,
                                        CarStatus status,
                                        Long mileage,
                                        BigDecimal price,
                                        Pageable pageable) {
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
                    criteriaBuilder.equal(root.get("year"), year));  // Використовуємо equal замість like
        }
        if (mileage != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("mileage"), mileage));  // Використовуємо equal замість like
        }
        if (price != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("price"), price));  // Використовуємо equal замість like
        }
        if (status != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("status"), status));
        }

        // Отримуємо сторінку автомобілів за специфікацією
        Page<Car> cars = carRepository.findAll(specification, pageable);

        // Створюємо Page<CarDto> та перетворюємо кожен Car в CarDto
        return cars.map(car -> new CarDto(
                car.getId(),
                car.isDeleted(),
                car.getCreatedAt(),
                car.getUpdatedAt(),
                car.getBrand(),
                car.getModel(),
                car.getYear(),
                car.getLicensePlate(),
                car.getStatus(),
                car.getMileage(),
                car.getPrice()
        ));
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

