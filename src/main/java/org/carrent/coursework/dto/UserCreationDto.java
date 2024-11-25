package org.carrent.coursework.dto;

import jakarta.validation.constraints.*;
import org.carrent.coursework.enums.EmployeePosition;
import org.carrent.coursework.enums.Role;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.User}
 */

public record UserCreationDto(
        boolean deleted,
        Date createdAt,
        Date updatedAt,
        @NotNull @Size(max = 255) @NotEmpty @NotBlank String lastName,
        @NotNull @Size(max = 255) @NotEmpty @NotBlank String firstName,
        @NotNull @Size(max = 255) @NotEmpty @NotBlank String middleName,
        @Past Date dateOfBirth,
        @NotNull @Email(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$") @NotEmpty @NotBlank String email,
        @NotNull @NotEmpty @NotBlank String phoneNumber,
        @NotNull @NotEmpty @NotBlank String address,
        String username,
        @NotBlank
        String password,
        Role role,
        @Past
        LocalDate hireDate,
        EmployeePosition position
) implements Serializable {
}
