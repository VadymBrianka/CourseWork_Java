package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CarCreationDto;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.entity.Car;
import org.carrent.coursework.exception.CarNotFoundException;
import org.carrent.coursework.mapper.CarMapper;
import org.carrent.coursework.repository.CarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class CarService{
    private final CarRepository carRepository;
    private final CarMapper carMapper;
    public CarDto getById(Long Id){
        Car car = carRepository.findById(Id).orElseThrow(() -> new CarNotFoundException("Car not found"));
        return carMapper.toDto(car);
    }
    @Transactional
    public CarDto create(CarCreationDto car){

        return carMapper.toDto(carRepository.save(carMapper.toEntity(car)));
    }
}
