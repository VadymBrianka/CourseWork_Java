package org.carrent.coursework.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Table(name = "customers")
@Entity
@Getter
@Setter

@NoArgsConstructor
public class Customer extends Person{

    @Column(name = "license_number")
    private String licenseNumber;

    // Зв'язок із Order
    @OneToMany(mappedBy = "customer")
    private List<Order> orders;
}
