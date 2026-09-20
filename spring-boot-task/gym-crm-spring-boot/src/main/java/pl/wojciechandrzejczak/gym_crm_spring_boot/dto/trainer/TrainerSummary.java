package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer;


import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;

public record TrainerSummary(
        String username,
        String firstName,
        String lastName,
        TrainingTypeName specialization
) {
}