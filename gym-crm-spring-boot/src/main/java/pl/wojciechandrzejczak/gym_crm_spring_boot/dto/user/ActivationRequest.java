package org.example.dto.user;

import jakarta.validation.constraints.NotNull;

public record ActivationRequest(
        @NotNull Boolean isActive
) {
}