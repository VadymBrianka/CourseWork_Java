package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CustomerDto;
import org.carrent.coursework.dto.EmployeeCreationDto;
import org.carrent.coursework.dto.EmployeeDto;
import org.carrent.coursework.entity.Customer;
import org.carrent.coursework.entity.Employee;
import org.carrent.coursework.enums.EmployeePosition;
import org.carrent.coursework.exception.CarAlreadyExistsException;
import org.carrent.coursework.exception.EmployeeNotFoundException;
import org.carrent.coursework.mapper.EmployeeMapper;
import org.carrent.coursework.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
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

    public Page<EmployeeDto> getAll(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(employeeMapper::toDto);
    }

    public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with ID: " + id + " not found"));
        employeeMapper.partialUpdate(employeeDto, employee);
        Employee updatedEmployee = employeeRepository.save(employee);
        return employeeMapper.toDto(updatedEmployee);
    }


    @Transactional
    public EmployeeDto create(EmployeeCreationDto employeeCreationDto){

        // Перевірка на існування працівника з таким же даними
        if (employeeRepository.existsByPositionAndEmailOrPositionAndPhoneNumber(employeeCreationDto.position(), employeeCreationDto.email(), employeeCreationDto.position() , employeeCreationDto.phoneNumber())) {
            throw new CarAlreadyExistsException("Employee with email " + employeeCreationDto.email() + ", phone number " + employeeCreationDto.phoneNumber() + " and on position " + employeeCreationDto.position() + " already exists");
        }

        return employeeMapper.toDto(employeeRepository.save(employeeMapper.toEntity(employeeCreationDto)));
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> getSortedEmployees(String sortBy, String order, Pageable pageable) {
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Employee> employeesPage = employeeRepository.findAll(sortedPageable);
        return employeesPage.map(employeeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<EmployeeDto> getFilteredEmployees(String lastName,
                                                  String firstName,
                                                  String middleName,
                                                  Date dateOfBirth,
                                                  String email,
                                                  String phoneNumber,
                                                  String address,
                                                  EmployeePosition position,
                                                  Pageable pageable) {
        Specification<Employee> specification = Specification.where(null);

        if (lastName != null && !lastName.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
        }
        if (firstName != null && !firstName.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
        }
        if (middleName != null && !middleName.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("middleName")), "%" + middleName.toLowerCase() + "%"));
        }
        if (dateOfBirth != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("dateOfBirth"), dateOfBirth));
        }
        if (email != null && !email.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), "%" + phoneNumber.toLowerCase() + "%"));
        }
        if (address != null && !address.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), "%" + address.toLowerCase() + "%"));
        }
        if (position != null) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("position"), position));
        }

        // Отримуємо сторінку клієнтів за специфікацією
        Page<Employee> employees = employeeRepository.findAll(specification, pageable);

        return employees.map(employee -> new EmployeeDto(
                employee.getId(),
                employee.isDeleted(),
                employee.getCreatedAt(),
                employee.getUpdatedAt(),
                employee.getLastName(),
                employee.getFirstName(),
                employee.getMiddleName(),
                employee.getDateOfBirth(),
                employee.getEmail(),
                employee.getPhoneNumber(),
                employee.getAddress(),
                employee.getPosition()
        ));
    }

}
