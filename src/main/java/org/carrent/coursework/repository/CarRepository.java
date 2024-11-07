package org.carrent.coursework.repository;

import org.carrent.coursework.entity.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long>{

}
