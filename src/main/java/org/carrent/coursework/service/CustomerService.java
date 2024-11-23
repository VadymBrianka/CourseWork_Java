package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.dto.CustomerCreationDto;
import org.carrent.coursework.dto.CustomerDto;
import org.carrent.coursework.entity.Car;
import org.carrent.coursework.entity.Customer;
import org.carrent.coursework.exception.CarAlreadyExistsException;
import org.carrent.coursework.exception.CarNotFoundException;
import org.carrent.coursework.exception.CustomerNotFoundException;
import org.carrent.coursework.mapper.CustomerMapper;
import org.carrent.coursework.repository.CustomerRepository;
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
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerDto getById(Long id) {
        logger.info("Fetching customer by ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Customer with ID: {} not found", id);
                    return new CustomerNotFoundException("Customer not found");
                });
        logger.info("Successfully fetched customer with ID: {}", id);
        return customerMapper.toDto(customer);
    }

    public Page<CustomerDto> getAll(Pageable pageable) {
        logger.info("Fetching all customers with pagination: {}", pageable);
        return customerRepository.findAll(pageable)
                .map(customerMapper::toDto);
    }

    @Transactional
    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        logger.info("Updating customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Customer with ID: {} not found", id);
                    return new CustomerNotFoundException("Customer with ID: " + id + " not found");
                });
        customerMapper.partialUpdate(customerDto, customer);
        Customer updatedCustomer = customerRepository.save(customer);
        logger.info("Successfully updated customer with ID: {}", id);
        return customerMapper.toDto(updatedCustomer);
    }

    @Transactional
    public CustomerDto create(CustomerCreationDto customerCreationDto) {
        logger.info("Creating a new customer with license number: {}", customerCreationDto.licenseNumber());
        if (customerRepository.existsByLicenseNumber(customerCreationDto.licenseNumber())) {
            logger.error("Customer with license number {} already exists", customerCreationDto.licenseNumber());
            throw new CarAlreadyExistsException("Customer with license number " + customerCreationDto.licenseNumber() + " already exists");
        }
        Customer customer = customerMapper.toEntity(customerCreationDto);
        Customer savedCustomer = customerRepository.save(customer);
        logger.info("Successfully created customer with ID: {}", savedCustomer.getId());
        return customerMapper.toDto(savedCustomer);
    }

    @Transactional
    public String deleteCustomer(Long id) {
        logger.info("Attempting to delete customer with ID: {}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Customer with ID: {} not found for deletion", id);
                    return new CustomerNotFoundException("Customer with ID: " + id + " not found");
                });
        customer.setDeleted(true);
        customerRepository.save(customer);
        logger.info("Successfully marked customer with ID: {} as deleted", id);
        return "Customer with ID " + id + " has been deleted.";
    }

    public Page<CustomerDto> getAllAvailable(Pageable pageable) {
        logger.info("Fetching all available customers with pagination: {}", pageable);
        Page<Customer> customers = customerRepository.findAll(pageable);
        Page<CustomerDto> availableCustomers = customers.stream()
                .filter(customer -> !customer.isDeleted())
                .map(customerMapper::toDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        list -> new PageImpl<>(list, pageable, customers.getTotalElements())));
        logger.info("Successfully fetched {} available customers", availableCustomers.getTotalElements());
        return availableCustomers;
    }




    public Page<CustomerDto> getSortedCustomers(String sortBy, String order, Pageable pageable) {
        logger.info("Fetching sorted customers by {} in {} order", sortBy, order);
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Customer> customersPage = customerRepository.findAll(sortedPageable);
        logger.info("Successfully fetched sorted customers");
        return customersPage.map(customerMapper::toDto);
    }


    public Page<CustomerDto> getFilteredCustomers(String lastName, String firstName, String middleName, Date dateOfBirth,
                                                  String email, String phoneNumber, String address, String licenseNumber,
                                                  Pageable pageable) {
        logger.info("Filtering customers with criteria - LastName: {}, FirstName: {}, MiddleName: {}, DateOfBirth: {}, Email: {}, Phone: {}, Address: {}, LicenseNumber: {}",
                lastName, firstName, middleName, dateOfBirth, email, phoneNumber, address, licenseNumber);
        Specification<Customer> specification = Specification.where(null);

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
        if (licenseNumber != null && !licenseNumber.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("licenseNumber")), "%" + licenseNumber.toLowerCase() + "%"));
        }

        Page<Customer> customers = customerRepository.findAll(specification, pageable);
        logger.info("Successfully filtered customers");
        return customers.map(customerMapper::toDto);
    }
}
