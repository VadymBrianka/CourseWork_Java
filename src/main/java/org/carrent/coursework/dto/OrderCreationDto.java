package org.carrent.coursework.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.carrent.coursework.enums.OrderStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.Order}
 */
public record OrderCreationDto(boolean deleted, Date createdAt, Date updatedAt, @NotNull LocalDate startDate,
                               @NotNull LocalDate endDate, @NotNull OrderStatus status,
                               @PositiveOrZero BigDecimal cost) implements Serializable {
}