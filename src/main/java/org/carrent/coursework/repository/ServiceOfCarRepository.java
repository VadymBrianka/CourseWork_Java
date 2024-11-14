package org.carrent.coursework.repository;

import org.carrent.coursework.entity.ServiceOfCar;
import org.carrent.coursework.enums.EmployeePosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface ServiceOfCarRepository extends JpaRepository<ServiceOfCar, Long> {

    boolean existsByCar_IdAndStartDateAndEndDateAndDescription(Long carId, LocalDate startDate, LocalDate endDate, String description);

}