package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.user;

public record RegistrationResponse(
        String username,
        String password
) {
}