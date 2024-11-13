package org.carrent.coursework.repository;

import org.carrent.coursework.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}