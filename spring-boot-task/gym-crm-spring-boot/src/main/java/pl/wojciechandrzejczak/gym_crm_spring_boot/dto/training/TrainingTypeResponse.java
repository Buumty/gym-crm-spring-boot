package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training;


import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;

public record TrainingTypeResponse(
        Long trainingTypeId,
        TrainingTypeName trainingType
) {
}