package org.example.dto.training;

import org.example.model.TrainingTypeName;

import java.time.LocalDate;

public record TrainerTrainingResponse(
        String trainingName,
        LocalDate trainingDate,
        TrainingTypeName trainingType,
        Integer trainingDuration,
        String traineeName
) {
}