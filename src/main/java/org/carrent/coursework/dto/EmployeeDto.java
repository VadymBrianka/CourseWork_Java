package org.carrent.coursework.dto;

import org.carrent.coursework.enums.EmployeePosition;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.Employee}
 */
public record EmployeeDto(Long id,
                          boolean deleted,
                          Date createdAt,
                          Date updatedAt,
                          String lastName,
                          String firstName,
                          String middleName,
                          Date dateOfBirth,
                          String email,
                          String phoneNumber,
                          String address,
                          EmployeePosition position
    ) implements Serializable {
}