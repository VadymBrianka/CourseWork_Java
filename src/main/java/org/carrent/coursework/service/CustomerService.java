package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CarDto;
import org.carrent.coursework.dto.CustomerCreationDto;
import org.carrent.coursework.dto.CustomerDto;
import org.carrent.coursework.entity.Car;
import org.carrent.coursework.entity.Customer;
import org.carrent.coursework.enums.CarStatus;
import org.carrent.coursework.exception.CarAlreadyExistsException;
import org.carrent.coursework.exception.CustomerNotFoundException;
import org.carrent.coursework.mapper.CustomerMapper;
import org.carrent.coursework.repository.CustomerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class CustomerService{
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    public CustomerDto getById(Long id){
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        return customerMapper.toDto(customer);
    }

    public Page<CustomerDto> getAll(Pageable pageable) {
        return customerRepository.findAll(pageable) // Використовуємо пагінацію
                .map(customerMapper::toDto); // Перетворюємо кожен Customer в CustomerDto
    }

    public CustomerDto updateCustomer(Long id, CustomerDto customerDto) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer with ID: " + id + " not found"));
        customerMapper.partialUpdate(customerDto, customer);
        Customer updatedCustomer = customerRepository.save(customer);
        return customerMapper.toDto(updatedCustomer);
    }


    @Transactional
    public CustomerDto create(CustomerCreationDto customerCreationDto){

        // Перевірка на існування клієнта з таким же licenseNumber
        if (customerRepository.existsByLicenseNumber(customerCreationDto.licenseNumber())) {
            throw new CarAlreadyExistsException("Customer with license number " + customerCreationDto.licenseNumber() + " already exists");
        }

        return customerMapper.toDto(customerRepository.save(customerMapper.toEntity(customerCreationDto)));
    }

    @Transactional(readOnly = true)
    public Page<CustomerDto> getSortedCustomers(String sortBy, String order, Pageable pageable) {
        Sort sort = order.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Customer> customersPage = customerRepository.findAll(sortedPageable);
        return customersPage.map(customerMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<CustomerDto> getFilteredCustomers(String lastName, String firstName, String middleName, Date dateOfBirth,
                                                  String email, String phoneNumber, String address, String licenseNumber,
                                                  Pageable pageable) {
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

        // Отримуємо сторінку клієнтів за специфікацією
        Page<Customer> customers = customerRepository.findAll(specification, pageable);

        return customers.map(customer -> new CustomerDto(
                customer.getId(),
                customer.isDeleted(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getLastName(),
                customer.getFirstName(),
                customer.getMiddleName(),
                customer.getDateOfBirth(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getAddress(),
                customer.getLicenseNumber()
        ));
    }


}
