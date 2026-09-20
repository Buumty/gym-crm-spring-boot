package org.example.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record TraineeTrainersUpdateRequest(
        @NotNull Set<@NotBlank String> trainerUsernames
) {
}