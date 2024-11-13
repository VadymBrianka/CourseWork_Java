package org.carrent.coursework.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.carrent.coursework.enums.EmployeePosition;


import java.util.List;

@Table(name = "employees")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Employee extends Person{

    @Enumerated(EnumType.STRING)
    @Column(name = "position")
    private EmployeePosition position;

    // Зв'язок із Order
    @OneToMany(mappedBy = "employee")
    private List<Order> orders;

    // Зв'язок із Service
    @OneToMany(mappedBy = "employee")
    private List<ServiceOfCar> services;
}
