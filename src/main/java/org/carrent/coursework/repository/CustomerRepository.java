package org.carrent.coursework.repository;

import org.carrent.coursework.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByLicenseNumber(String licensePlate);

}