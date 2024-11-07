package org.carrent.coursework.mapper;

import org.carrent.coursework.dto.CarCreationDto;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.entity.Car;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CarMapper {
    @Mapping(source = "modelka", target = "model")
    Car toEntity(CarDto carDto);

    @Mapping(source = "model", target = "modelka")
    CarDto toDto(Car car);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "modelka", target = "model")
    Car partialUpdate(CarDto carDto, @MappingTarget Car car);

    Car toEntity(CarCreationDto carCreationDto);

}