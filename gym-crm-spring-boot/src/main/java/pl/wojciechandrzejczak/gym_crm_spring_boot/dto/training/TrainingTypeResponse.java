package org.example.dto.training;

import org.example.model.TrainingTypeName;

public record TrainingTypeResponse(
        Long trainingTypeId,
        TrainingTypeName trainingType
) {
}