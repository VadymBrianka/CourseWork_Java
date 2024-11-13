package org.carrent.coursework.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.carrent.coursework.enums.ServiceStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.Service}
 */
public record ServiceCreationDto(boolean deleted, Date createdAt, Date updatedAt, @NotNull LocalDate startDate,
                                 @NotNull LocalDate endDate, @NotNull String description, @PositiveOrZero double cost,
                                 @NotNull ServiceStatus status) implements Serializable {
}