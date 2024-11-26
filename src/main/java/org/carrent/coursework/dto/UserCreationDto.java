package org.carrent.coursework.dto;

import jakarta.validation.constraints.*;
import org.carrent.coursework.enums.Role;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.User}
 */

public record UserCreationDto(
        boolean deleted,
        Date createdAt,
        Date updatedAt,
        String username,
        @NotBlank
        String password,
        Role role
) implements Serializable {
}
