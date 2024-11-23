package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.EmployeeCreationDto;
import org.carrent.coursework.dto.EmployeeDto;
import org.carrent.coursework.entity.Employee;
import org.carrent.coursework.enums.EmployeePosition;
import org.carrent.coursework.exception.CarAlreadyExistsException;
import org.carrent.coursework.exception.EmployeeNotFoundException;
import org.carrent.coursework.mapper.EmployeeMapper;
import org.carrent.coursework.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class EmployeeService {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeDto getById(Long id) {
        logger.info("Fetching employee with ID: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));
        logger.debug("Fetched employee: {}", employee);
        return employeeMapper.toDto(employee);
    }

    public Page<EmployeeDto> getAll(Pageable pageable) {
        logger.info("Fetching all employees with pagination: {}", pageable);
        Page<EmployeeDto> employees = employeeRepository.findAll(pageable)
                .map(employeeMapper::toDto);
        logger.debug("Fetched employees page: {}", employees);
        return employees;
    }

    public Page<EmployeeDto> getAllAvailable(Pageable pageable) {
        logger.info("Fetching all available employees with pagination: {}", pageable);
        Page<Employee> employees = employeeRepository.findAll(pageable);
        logger.debug("Filtered out deleted employees from the list");
        return employees.stream()
                .filter(employee -> !employee.isDeleted())
                .map(employeeMapper::toDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> new PageImpl<>(list, pageable, employees.getTotalElements())));
    }

    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {
        logger.info("Updating employee with ID: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with ID: " + id + " not found"));
        logger.debug("Existing employee: {}", employee);
        employeeMapper.partialUpdate(employeeDto, employee);
        Employee updatedEmployee = employeeRepository.save(employee);
        logger.info("Employee with ID: {} successfully updated", id);
        return employeeMapper.toDto(updatedEmployee);
    }

    @Transactional
    public EmployeeDto create(EmployeeCreationDto employeeCreationDto) {
        logger.info("Creating a new employee with email: {} and phone number: {}", employeeCreationDto.email(), employeeCreationDto.phoneNumber());

        if (employeeRepository.existsByPositionAndEmailOrPositionAndPhoneNumber(
                employeeCreationDto.position(), employeeCreationDto.email(),
                employeeCreationDto.position(), employeeCreationDto.phoneNumber())) {
            logger.error("Employee with email {} and phone number {} already exists", employeeCreationDto.email(), employeeCreationDto.phoneNumber());
            throw new CarAlreadyExistsException("Employee with email " + employeeCreationDto.email() +
                    ", phone number " + employeeCreationDto.phoneNumber() +
                    " and on position " + employeeCreationDto.position() + " already exists");
        }

        Employee employee = employeeMapper.toEntity(employeeCreationDto);
        Employee savedEmployee = employeeRepository.save(employee);
        logger.info("New employee created with ID: {}", savedEmployee.getId());
        return employeeMapper.toDto(savedEmployee);
    }

    public Page<EmployeeDto> getSortedEmployees(String sortBy, String order, Pageable pageable) {
        logger.info("Fetching sorted employees by {} in {} order", sortBy, order);
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Employee> employeesPage = employeeRepository.findAll(sortedPageable);
        logger.debug("Fetched sorted employees page: {}", employeesPage);
        return employeesPage.map(employeeMapper::toDto);
    }

    public Page<EmployeeDto> getFilteredEmployees(String lastName, String firstName, String middleName, Date dateOfBirth,
                                                  String email, String phoneNumber, String address, EmployeePosition position,
                                                  Pageable pageable) {
        logger.info("Filtering employees with provided criteria");
        Specification<Employee> specification = Specification.where(null);

        if (lastName != null && !lastName.isEmpty()) {
            logger.debug("Filtering by lastName: {}", lastName);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
        }
        if (firstName != null && !firstName.isEmpty()) {
            logger.debug("Filtering by firstName: {}", firstName);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), "%" + firstName.toLowerCase() + "%"));
        }
        if (middleName != null && !middleName.isEmpty()) {
            logger.debug("Filtering by middleName: {}", middleName);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("middleName")), "%" + middleName.toLowerCase() + "%"));
        }
        if (dateOfBirth != null) {
            logger.debug("Filtering by dateOfBirth: {}", dateOfBirth);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("dateOfBirth"), dateOfBirth));
        }
        if (email != null && !email.isEmpty()) {
            logger.debug("Filtering by email: {}", email);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            logger.debug("Filtering by phoneNumber: {}", phoneNumber);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("phoneNumber")), "%" + phoneNumber.toLowerCase() + "%"));
        }
        if (address != null && !address.isEmpty()) {
            logger.debug("Filtering by address: {}", address);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("address")), "%" + address.toLowerCase() + "%"));
        }
        if (position != null) {
            logger.debug("Filtering by position: {}", position);
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("position"), position));
        }

        Page<Employee> employees = employeeRepository.findAll(specification, pageable);
        logger.info("Filtered employees fetched successfully");
        return employees.map(employeeMapper::toDto);
    }

    @Transactional
    public String deleteEmployee(Long id) {
        logger.info("Deleting employee with ID: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with ID: " + id + " not found"));
        employee.setDeleted(true);
        employeeRepository.save(employee);
        logger.info("Employee with ID: {} has been deleted", id);
        return "Employee with ID " + id + " has been deleted.";
    }
}