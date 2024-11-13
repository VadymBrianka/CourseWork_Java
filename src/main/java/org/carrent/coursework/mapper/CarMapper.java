package org.carrent.coursework.mapper;

import org.carrent.coursework.dto.CarCreationDto;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.entity.Car;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CarMapper {
    Car toEntity(CarDto carDto);

    CarDto toDto(Car car);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Car partialUpdate(CarDto carDto, @MappingTarget Car car);

    Car toEntity(CarCreationDto carCreationDto);
}