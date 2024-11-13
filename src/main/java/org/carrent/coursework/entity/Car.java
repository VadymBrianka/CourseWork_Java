package org.carrent.coursework.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.carrent.coursework.enums.CarStatus;

import java.math.BigDecimal;
import java.util.List;

@Table(name = "cars")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Car extends BaseEntity{

    @Column(name = "brand")
    private String brand;

    @Column(name = "model")
    private String model;

    @Column(name = "year")
    private int year;

    @Column(name = "license_plate")
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CarStatus status;

    @Column(name = "mileage")
    private Long mileage;

    @Column(name = "price")
    private BigDecimal price;

    // Зв'язок із Order
    @OneToMany(mappedBy = "car")
    private List<Order> orders;

    // Зв'язок із Service
    @OneToMany(mappedBy = "car")
    private List<ServiceOfCar> services;
}
