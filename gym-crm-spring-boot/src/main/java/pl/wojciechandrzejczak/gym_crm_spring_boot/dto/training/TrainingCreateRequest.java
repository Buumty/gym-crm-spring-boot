package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record TrainingCreateRequest(
        @NotBlank String traineeUsername,
        @NotBlank String trainerUsername,
        @NotBlank String trainingName,
        @NotNull LocalDate trainingDate,
        @NotNull @Positive Integer trainingDuration
) {
}