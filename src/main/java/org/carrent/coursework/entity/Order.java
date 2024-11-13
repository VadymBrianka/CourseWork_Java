package org.carrent.coursework.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.carrent.coursework.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "orders")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Order extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "employee_id")  // Співробітник, що обробляє бронювання
    private Employee employee;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;

    @Column(name = "cost")
    private BigDecimal cost;
}
