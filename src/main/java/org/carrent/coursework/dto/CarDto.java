package org.carrent.coursework.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.carrent.coursework.enums.CarStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.Car}
 */
public record CarDto(
                     Long id,
                     boolean deleted,
                     Date createdAt,
                     Date updatedAt,
                     String brand,
                     String model,
                     int year,
                     String licensePlate,
                     CarStatus status,
                     Long mileage,
                     BigDecimal price
                ) implements Serializable {
}