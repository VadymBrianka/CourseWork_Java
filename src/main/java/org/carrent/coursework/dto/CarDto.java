package org.carrent.coursework.dto;

import java.io.Serializable;

/**
 * DTO for {@link org.carrent.coursework.entity.Car}
 */
public record CarDto(Long id, String brand, String modelka, int year, int price) implements Serializable {
}