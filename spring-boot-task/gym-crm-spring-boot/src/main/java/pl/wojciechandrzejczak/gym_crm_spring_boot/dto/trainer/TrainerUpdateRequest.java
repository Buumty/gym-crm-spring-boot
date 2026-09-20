package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TrainerUpdateRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull Boolean isActive
) {
}