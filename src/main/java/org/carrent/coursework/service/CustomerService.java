package org.carrent.coursework.service;

import lombok.AllArgsConstructor;
import org.carrent.coursework.dto.CustomerCreationDto;
import org.carrent.coursework.dto.CustomerDto;
import org.carrent.coursework.entity.Customer;
import org.carrent.coursework.exception.CustomerNotFoundException;
import org.carrent.coursework.mapper.CustomerMapper;
import org.carrent.coursework.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<CustomerDto> getAll() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(customerMapper::toDto)
                .toList();
    }

    @Transactional
    public CustomerDto create(CustomerCreationDto customer){

        return customerMapper.toDto(customerRepository.save(customerMapper.toEntity(customer)));
    }
}
