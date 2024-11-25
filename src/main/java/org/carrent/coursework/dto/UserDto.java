package org.carrent.coursework.dto;

import jakarta.annotation.Nullable;
import org.carrent.coursework.enums.Role;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * DTO for {@link org.carrent.coursework.entity.User}
 */
public record UserDto(Long id,
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
                      String username,
                      String password,
                      Role role
        /*@Nullable LocalDate hireDate,
        @Nullable String position*/) implements Serializable {
//    /**
//     * DTO for {@link org.carrent.coursework.entity.InsurancePolicy}
//     */
//    public record InsurancePolicyDto1(Long id, String policyNumber) implements Serializable {
//    }
}