package org.carrent.coursework.repository;

import org.carrent.coursework.entity.Employee;
import org.carrent.coursework.entity.Order;
import org.carrent.coursework.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    boolean existsByCar_IdAndStartDateAndEndDate(Long carId, LocalDateTime startDate, LocalDateTime endDate);
    Optional<Order> findFirstByCar_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long carId, LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByCar_IdAndStartDateGreaterThanOrderByStartDateAsc(Long carId, LocalDateTime today);

//    Optional<Order> findFirstByCar_IdAndEndDateGreaterThanOrStartDateLessThanAndStatusIsNotLike(Long carId, LocalDateTime startDate, LocalDateTime endDate, OrderStatus orderStatus);

//    Optional<Order> findFirstByCar_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndStatusIn(
//        Long carId, LocalDateTime endDate, LocalDateTime startDate, List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o " +
            "WHERE o.car.id = :carId " +
            "AND o.status IN :statuses " +
            "AND (" +
            "   (o.startDate <= :startDate AND o.endDate >= :endDate)" + // Перекриття
            "   OR (o.startDate BETWEEN :startDate AND :endDate)" + // Початок під час нового запису
            "   OR (o.endDate BETWEEN :startDate AND :endDate)" + // Кінець під час нового запису
            ")")
    Optional<Order> findFirstByCarAndDateRangeAndStatuses(
            @Param("carId") Long carId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("statuses") List<OrderStatus> statuses);


    Optional<Order> findFirstByCar_IdAndStartDateEquals(Long carId, LocalDateTime date);
}
