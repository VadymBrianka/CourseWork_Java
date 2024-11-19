package org.carrent.coursework.repository;

import org.carrent.coursework.entity.Employee;
import org.carrent.coursework.entity.Order;
import org.carrent.coursework.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    boolean existsByCar_IdAndStartDateAndEndDate(Long carId, LocalDateTime startDate, LocalDateTime endDate);
    Optional<Order> findFirstByCar_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long carId, LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByCar_IdAndStartDateGreaterThanOrderByStartDateAsc(Long carId, LocalDateTime today);

//    Optional<Order> findFirstByCar_IdAndEndDateGreaterThanOrStartDateLessThanAndStatusIsNotLike(Long carId, LocalDateTime startDate, LocalDateTime endDate, OrderStatus orderStatus);

    Optional<Order> findFirstByCar_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatusIn(
        Long carId, LocalDateTime endDate, LocalDateTime startDate, List<OrderStatus> statuses);


    Optional<Order> findFirstByCar_IdAndStartDateEquals(Long carId, LocalDateTime date);
}
