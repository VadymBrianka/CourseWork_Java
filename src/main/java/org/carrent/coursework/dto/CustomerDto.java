package org.carrent.coursework.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.Customer}
 */
public record CustomerDto(
                          Long id,
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
                          String licenseNumber
                ) implements Serializable {
}