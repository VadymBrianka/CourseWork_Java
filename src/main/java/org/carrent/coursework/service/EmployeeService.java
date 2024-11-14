package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.EmployeeCreationDto;
import org.carrent.coursework.dto.EmployeeDto;
import org.carrent.coursework.entity.Employee;
import org.carrent.coursework.exception.CarAlreadyExistsException;
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
    public EmployeeDto create(EmployeeCreationDto employeeCreationDto){

        // Перевірка на існування працівника з таким же даними
        if (employeeRepository.existsByPositionAndEmailOrPositionAndPhoneNumber(employeeCreationDto.position(), employeeCreationDto.email(), employeeCreationDto.position() , employeeCreationDto.phoneNumber())) {
            throw new CarAlreadyExistsException("Customer with email " + employeeCreationDto.email() + ", phone number " + employeeCreationDto.phoneNumber() + " and on position " + employeeCreationDto.position() + " already exists");
        }

        return employeeMapper.toDto(employeeRepository.save(employeeMapper.toEntity(employeeCreationDto)));
    }
}
