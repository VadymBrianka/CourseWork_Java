package org.carrent.coursework.repository;

import org.carrent.coursework.entity.Employee;
import org.carrent.coursework.enums.EmployeePosition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    boolean existsByPositionAndEmailOrPositionAndPhoneNumber(EmployeePosition position1, String email, EmployeePosition position2, String phoneNumber);
}