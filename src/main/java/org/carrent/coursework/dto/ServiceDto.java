package org.carrent.coursework.dto;

import org.carrent.coursework.enums.ServiceStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.Service}
 */
public record ServiceDto(Long id, boolean deleted, Date createdAt, Date updatedAt, Long carId, Long employeeId,
                         LocalDate startDate, LocalDate endDate, String description, double cost,
                         ServiceStatus status) implements Serializable {
}