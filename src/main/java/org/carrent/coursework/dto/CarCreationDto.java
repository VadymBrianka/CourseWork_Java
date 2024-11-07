package org.carrent.coursework.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for {@link org.carrent.coursework.entity.Car}
 */
public record CarCreationDto(
        @Size(message = "Brand name too long", max = 255)
        @NotBlank(message = "Brand can not be blank")
        String brand,

        @NotBlank String model,
        int year,

        @Positive(message = "Price can not be ") int price) implements Serializable {
}