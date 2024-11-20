package org.carrent.coursework.repository;

import org.carrent.coursework.entity.Customer;
import org.carrent.coursework.entity.Order;
import org.carrent.coursework.entity.ServiceOfCar;
import org.carrent.coursework.enums.EmployeePosition;
import org.carrent.coursework.enums.OrderStatus;
import org.carrent.coursework.enums.ServiceOfCarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ServiceOfCarRepository extends JpaRepository<ServiceOfCar, Long>, JpaSpecificationExecutor<ServiceOfCar> {

    boolean existsByCar_IdAndStartDateAndEndDateAndDescription(Long carId, LocalDateTime startDate, LocalDateTime endDate, String description);

    List<Order> findByCar_IdAndStartDateGreaterThan(Long carId, LocalDateTime today);
    Optional <ServiceOfCar> findFirstByCar_IdAndEndDateGreaterThanEqual(Long carId, LocalDateTime today);

//    Optional<ServiceOfCar> findFirstByCar_IdAndEndDateGreaterThanOrStartDateLessThan(Long carId, LocalDateTime endDate, LocalDateTime startDate);

    @Query("SELECT s FROM ServiceOfCar s " +
            "WHERE s.car.id = :carId " +
            "AND (s.endDate > :endDate OR s.startDate < :startDate) " +
            "ORDER BY s.id ASC")
    Optional<ServiceOfCar> findFirstByCarIdAndDateRange(
            @Param("carId") Long carId,
            @Param("endDate") LocalDateTime endDate,
            @Param("startDate") LocalDateTime startDate);



}