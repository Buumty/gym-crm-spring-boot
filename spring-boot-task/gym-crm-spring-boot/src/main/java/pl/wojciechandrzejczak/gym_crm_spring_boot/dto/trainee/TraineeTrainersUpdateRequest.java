package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record TraineeTrainersUpdateRequest(
        @NotNull Set<@NotBlank String> trainerUsernames
) {
}