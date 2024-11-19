package org.carrent.coursework.repository;

import org.carrent.coursework.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CarRepository extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {

    boolean existsByLicensePlate(String licensePlate);

    boolean existsById(Long carId);
}
