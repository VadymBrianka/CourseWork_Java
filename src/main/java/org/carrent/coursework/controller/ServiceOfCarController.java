package org.carrent.coursework.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.ServiceOfCarCreationDto;
import org.carrent.coursework.dto.ServiceOfCarDto;
import org.carrent.coursework.service.ServiceOfCarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@AllArgsConstructor
public class ServiceOfCarController
{
    private final ServiceOfCarService serviceOfCarService;

    @GetMapping("{id}")
    public ResponseEntity<ServiceOfCarDto> getServiceById(@PathVariable Long id){
        return ResponseEntity.ok(serviceOfCarService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ServiceOfCarDto>> getAllServices(){
        return ResponseEntity.ok(serviceOfCarService.getAll());
    }

    @PostMapping
    public ResponseEntity<ServiceOfCarDto> createService(@Valid @RequestBody ServiceOfCarCreationDto serviceOfCarCreationDto){
        return new ResponseEntity<>(serviceOfCarService.create(serviceOfCarCreationDto), HttpStatus.CREATED);
    }
}
