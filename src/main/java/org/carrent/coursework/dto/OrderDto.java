package org.carrent.coursework.dto;

import org.carrent.coursework.enums.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.Order}
 */
public record OrderDto(Long id,
                       boolean deleted,
                       Date createdAt,
                       Date updatedAt,
                       Long carId,
                       Long customerId,
                       Long employeeId,
                       LocalDateTime startDate,
                       LocalDateTime endDate,
                       OrderStatus status,
                       BigDecimal cost
    ) implements Serializable {
}