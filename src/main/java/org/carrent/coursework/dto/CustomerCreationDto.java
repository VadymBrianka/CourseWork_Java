package org.carrent.coursework.dto;

import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.Customer}
 */
public record CustomerCreationDto(boolean deleted, Date createdAt, Date updatedAt,
                                  @NotNull @Size(max = 255) @NotEmpty @NotBlank String lastName,
                                  @NotNull @Size(max = 255) @NotEmpty @NotBlank String firstName,
                                  @NotNull @Size(max = 255) @NotEmpty @NotBlank String middleName,
                                  @NotNull @Past Date dateOfBirth,
                                  @NotNull @Email( regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") @NotEmpty @NotBlank String email,
                                  @NotNull @Size(max = 255) @NotEmpty @NotBlank String phoneNumber,
                                  @NotNull @NotEmpty @NotBlank String address,
                                  @NotNull @Size(max = 255) @NotEmpty @NotBlank String licenseNumber) implements Serializable {
}