package org.carrent.coursework.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.carrent.coursework.enums.ServiceOfCarStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.ServiceOfCar}
 */
public record ServiceOfCarCreationDto(
                                      boolean deleted,
                                      @NotNull Long carId,
                                      @NotNull Long employeeId,
                                      Date createdAt,
                                      Date updatedAt,
                                      @NotNull LocalDateTime startDate,
                                      @NotNull LocalDateTime endDate,
                                      @NotNull String description,
                                      @PositiveOrZero BigDecimal cost,
                                      ServiceOfCarStatus status

) implements Serializable { }