package org.carrent.coursework.dto;

import jakarta.validation.constraints.*;
import org.carrent.coursework.enums.CarStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.Car}
 */
public record CarCreationDto(boolean deleted, Date createdAt, Date updatedAt,
                             @NotNull @Size(max = 255) @NotEmpty @NotBlank String brand,
                             @NotNull @Size(max = 255) @NotEmpty @NotBlank String model, @Positive int year,
                             @NotNull @Size(max = 255) @NotEmpty @NotBlank String licensePlate,
                             @NotNull CarStatus status, @NotNull @Positive Long mileage,
                             @Positive BigDecimal price) implements Serializable {
}