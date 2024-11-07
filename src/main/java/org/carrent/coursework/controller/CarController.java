package org.carrent.coursework.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CarCreationDto;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.entity.Car;
import org.carrent.coursework.service.CarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cars")
@AllArgsConstructor
public class CarController
{
    private final CarService carService;

    @GetMapping("{id}")
    public ResponseEntity<CarDto> getCarById(@PathVariable Long id){
        return ResponseEntity.ok(carService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CarDto> createCar(@Valid @RequestBody CarCreationDto carCreationDto){
        return new ResponseEntity(carService.create(carCreationDto), HttpStatus.CREATED);
    }

}

