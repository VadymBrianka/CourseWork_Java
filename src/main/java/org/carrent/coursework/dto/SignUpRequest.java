package org.carrent.coursework.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {
    @Size(min = 5, max = 50)
    @NotBlank
    private String username;

    @Size(max = 255)
    private String password;

}
