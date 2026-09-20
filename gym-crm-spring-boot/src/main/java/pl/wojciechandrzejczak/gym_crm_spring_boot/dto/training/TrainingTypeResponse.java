package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training;

import org.example.model.TrainingTypeName;

public record TrainingTypeResponse(
        Long trainingTypeId,
        TrainingTypeName trainingType
) {
}