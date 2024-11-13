package org.carrent.coursework.repository;

import org.carrent.coursework.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}