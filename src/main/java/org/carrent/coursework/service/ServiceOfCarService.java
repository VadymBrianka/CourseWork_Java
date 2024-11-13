package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.ServiceOfCarCreationDto;
import org.carrent.coursework.dto.ServiceOfCarDto;
import org.carrent.coursework.entity.ServiceOfCar;
import org.carrent.coursework.exception.ServiceOfCarNotFoundException;
import org.carrent.coursework.mapper.ServiceOfCarMapper;
import org.carrent.coursework.repository.ServiceOfCarRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class ServiceOfCarService {
    private final ServiceOfCarRepository serviceOfCarRepository;
    private final ServiceOfCarMapper serviceOfCarMapper;
    public ServiceOfCarDto getById(Long id){
        ServiceOfCar serviceOfCar = serviceOfCarRepository.findById(id).orElseThrow(() -> new ServiceOfCarNotFoundException("Service not found"));
        return serviceOfCarMapper.toDto(serviceOfCar);
    }

    public List<ServiceOfCarDto> getAll() {
        List<ServiceOfCar> servicesOfCar = serviceOfCarRepository.findAll();
        return servicesOfCar.stream()
                .map(serviceOfCarMapper::toDto)
                .toList();
    }

    @Transactional
    public ServiceOfCarDto create(ServiceOfCarCreationDto serviceOfCar){

        return serviceOfCarMapper.toDto(serviceOfCarRepository.save(serviceOfCarMapper.toEntity(serviceOfCar)));
    }
}
