package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training;

import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;

import java.time.LocalDate;

public record TraineeTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        TrainingTypeName trainingType,
        Integer trainingDuration,
        String trainerName
) {
}