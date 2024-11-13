package org.carrent.coursework.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.carrent.coursework.enums.ServiceOfCarStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table(name = "services")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ServiceOfCar extends BaseEntity{

    @ManyToOne
    @JoinColumn(name = "car_id")
    private Car car;

    @ManyToOne
    @JoinColumn(name = "employee_id")  // Співробітник, що виконує обслуговування
    private Employee employee;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "description")
    private String description;

    @Column(name = "cost")
    private BigDecimal cost;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ServiceOfCarStatus status;
}
