package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.user;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank String username,
        @NotBlank String oldPassword,
        @NotBlank String newPassword
) {
}