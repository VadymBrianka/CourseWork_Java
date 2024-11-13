package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CarCreationDto;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.entity.Car;
import org.carrent.coursework.exception.CarAlreadyExistsException;
import org.carrent.coursework.exception.CarNotFoundException;
import org.carrent.coursework.mapper.CarMapper;
import org.carrent.coursework.repository.CarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class CarService {
    private final CarRepository carRepository;
    private final CarMapper carMapper;

    public CarDto getById(Long id) {
        Car car = carRepository.findById(id).orElseThrow(() -> new CarNotFoundException("Car not found"));
        return carMapper.toDto(car);
    }

    public List<CarDto> getAll() {
        List<Car> cars = carRepository.findAll();
        return cars.stream()
                .map(carMapper::toDto)
                .toList();
    }

    @Transactional
    public CarDto create(CarCreationDto carCreationDto) {

        // Перевірка на існування автомобіля з таким же licensePlate
        if (carRepository.existsByLicensePlate(carCreationDto.licensePlate())) {
            throw new CarAlreadyExistsException("Car with license plate " + carCreationDto.licensePlate() + " already exists");
        }
        return carMapper.toDto(carRepository.save(carMapper.toEntity(carCreationDto)));
    }
}
