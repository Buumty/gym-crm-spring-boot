package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.user;

import jakarta.validation.constraints.NotNull;

public record ActivationRequest(
        @NotNull Boolean isActive
) {
}