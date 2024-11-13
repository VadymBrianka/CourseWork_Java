package org.carrent.coursework.mapper;

import org.carrent.coursework.dto.EmployeeCreationDto;
import org.carrent.coursework.dto.EmployeeDto;
import org.carrent.coursework.entity.Employee;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface EmployeeMapper {
    Employee toEntity(EmployeeDto employeeDto);

    EmployeeDto toDto(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Employee partialUpdate(EmployeeDto employeeDto, @MappingTarget Employee employee);

    Employee toEntity(EmployeeCreationDto employeeCreationDto);
}