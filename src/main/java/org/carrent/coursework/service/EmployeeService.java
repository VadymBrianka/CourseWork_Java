package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.EmployeeCreationDto;
import org.carrent.coursework.dto.EmployeeDto;
import org.carrent.coursework.entity.Employee;
import org.carrent.coursework.exception.EmployeeNotFoundException;
import org.carrent.coursework.mapper.EmployeeMapper;
import org.carrent.coursework.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class EmployeeService{
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    public EmployeeDto getById(Long id){
        Employee employee = employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        return employeeMapper.toDto(employee);
    }

    public List<EmployeeDto> getAll() {
        List<Employee> customers = employeeRepository.findAll();
        return customers.stream()
                .map(employeeMapper::toDto)
                .toList();
    }

    @Transactional
    public EmployeeDto create(EmployeeCreationDto employee){

        return employeeMapper.toDto(employeeRepository.save(employeeMapper.toEntity(employee)));
    }
}
