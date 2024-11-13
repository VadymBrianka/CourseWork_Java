package org.carrent.coursework.mapper;

import org.carrent.coursework.dto.ServiceOfCarCreationDto;
import org.carrent.coursework.dto.ServiceOfCarDto;
import org.carrent.coursework.entity.ServiceOfCar;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceOfCarMapper {
    @Mapping(source = "employeeId", target = "employee.id")
    @Mapping(source = "carId", target = "car.id")
    ServiceOfCar toEntity(ServiceOfCarDto serviceDto);

    @InheritInverseConfiguration(name = "toEntity")
    ServiceOfCarDto toDto(ServiceOfCar service);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ServiceOfCar partialUpdate(ServiceOfCarDto serviceDto, @MappingTarget ServiceOfCar service);

    ServiceOfCar toEntity(ServiceOfCarCreationDto serviceCreationDto);
}