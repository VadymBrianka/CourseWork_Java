package org.carrent.coursework.mapper;

import org.carrent.coursework.dto.CustomerCreationDto;
import org.carrent.coursework.dto.CustomerDto;
import org.carrent.coursework.entity.Customer;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface CustomerMapper {
    Customer toEntity(CustomerDto customerDto);

    CustomerDto toDto(Customer customer);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Customer partialUpdate(CustomerDto customerDto, @MappingTarget Customer customer);

    Customer toEntity(CustomerCreationDto customerCreationDto);
}