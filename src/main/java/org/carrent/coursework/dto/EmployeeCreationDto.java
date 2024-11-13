package org.carrent.coursework.dto;

import jakarta.validation.constraints.*;
import org.carrent.coursework.enums.EmployeePosition;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.Employee}
 */
public record EmployeeCreationDto(boolean deleted, Date createdAt, Date updatedAt,
                                  @NotNull @Size(max = 255) @NotEmpty @NotBlank String lastName,
                                  @NotNull @Size(max = 255) @NotEmpty @NotBlank String firstName,
                                  @NotNull @Size(max = 255) @NotEmpty @NotBlank String middleName,
                                  @Past Date dateOfBirth,
                                  @NotNull @Email(regexp = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\\\.[A-Za-z0-9_-]+)*@\"+ \"[^-][A-Za-z0-9-]+(\\\\.[A-Za-z0-9-]+)*(\\\\.[A-Za-z]{2,})$") @NotEmpty @NotBlank String email,
                                  @NotNull @NotEmpty @NotBlank String phoneNumber,
                                  @NotNull @NotEmpty @NotBlank String address,
                                  @NotNull EmployeePosition position) implements Serializable {
}