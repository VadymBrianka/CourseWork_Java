package org.carrent.coursework.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.carrent.coursework.enums.ServiceOfCarStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.ServiceOfCar}
 */
public record ServiceOfCarCreationDto(boolean deleted, Date createdAt, Date updatedAt, @NotNull LocalDate startDate,
                                 @NotNull LocalDate endDate, @NotNull String description, @PositiveOrZero BigDecimal cost,
                                 @NotNull ServiceOfCarStatus status) implements Serializable {
}