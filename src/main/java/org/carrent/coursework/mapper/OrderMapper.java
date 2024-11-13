package org.carrent.coursework.mapper;

import org.carrent.coursework.dto.OrderCreationDto;
import org.carrent.coursework.dto.OrderDto;
import org.carrent.coursework.entity.Order;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    @Mapping(source = "employeeId", target = "employee.id")
    @Mapping(source = "customerId", target = "customer.id")
    @Mapping(source = "carId", target = "car.id")
    Order toEntity(OrderDto orderDto);

    @InheritInverseConfiguration(name = "toEntity")
    OrderDto toDto(Order order);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Order partialUpdate(OrderDto orderDto, @MappingTarget Order order);

    Order toEntity(OrderCreationDto orderCreationDto);
}