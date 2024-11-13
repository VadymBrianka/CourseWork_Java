package org.carrent.coursework.mapper;

import org.carrent.coursework.dto.ServiceCreationDto;
import org.carrent.coursework.dto.ServiceDto;
import org.carrent.coursework.entity.Service;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ServiceMapper {
    @Mapping(source = "employeeId", target = "employee.id")
    @Mapping(source = "carId", target = "car.id")
    Service toEntity(ServiceDto serviceDto);

    @InheritInverseConfiguration(name = "toEntity")
    ServiceDto toDto(Service service);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Service partialUpdate(ServiceDto serviceDto, @MappingTarget Service service);

    Service toEntity(ServiceCreationDto serviceCreationDto);
}