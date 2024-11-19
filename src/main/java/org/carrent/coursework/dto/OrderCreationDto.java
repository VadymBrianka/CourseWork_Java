package org.carrent.coursework.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.carrent.coursework.enums.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.Order}
 */
public record OrderCreationDto(
                               boolean deleted,
                               @NotNull Long carId,
                               @NotNull Long employeeId,
                               @NotNull Long customerId,
                               Date createdAt,
                               Date updatedAt,
                               @NotNull LocalDateTime startDate,
                               @NotNull LocalDateTime endDate,
//                               @NotNull
                               OrderStatus status,
                               @PositiveOrZero BigDecimal cost
) implements Serializable { }