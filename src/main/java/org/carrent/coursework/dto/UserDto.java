package org.carrent.coursework.dto;

import org.carrent.coursework.enums.Role;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO for {@link org.carrent.coursework.entity.User}
 */
public record UserDto(Long id,
                      boolean deleted,
                      Date createdAt,
                      Date updatedAt,
                      String username,
                      String password,
                      Role role) implements Serializable {
}