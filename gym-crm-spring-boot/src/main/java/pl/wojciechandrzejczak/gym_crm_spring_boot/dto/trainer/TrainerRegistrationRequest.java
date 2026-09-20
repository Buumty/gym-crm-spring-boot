package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.model.TrainingTypeName;

public record TrainerRegistrationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull TrainingTypeName specialization
) {
}