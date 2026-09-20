package org.example.dto.trainee;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record TraineeRegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        LocalDate dateOfBirth,
        String address
) {
}