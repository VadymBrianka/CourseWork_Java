package org.carrent.coursework.dto;

import org.carrent.coursework.enums.ServiceOfCarStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.ServiceOfCar}
 */
public record ServiceOfCarDto(Long id,
                              boolean deleted,
                              Date createdAt,
                              Date updatedAt,
                              Long carId,
                              Long employeeId,
                              LocalDateTime startDate,
                              LocalDateTime endDate,
                              String description,
                              BigDecimal cost,
                              ServiceOfCarStatus status
) implements Serializable { }