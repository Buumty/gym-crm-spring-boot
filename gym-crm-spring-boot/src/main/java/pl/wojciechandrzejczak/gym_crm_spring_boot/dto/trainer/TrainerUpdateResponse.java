package org.example.dto.trainer;

import org.example.dto.trainee.TraineeSummary;
import org.example.model.TrainingTypeName;

import java.util.List;

public record TrainerUpdateResponse(
        String username,
        String firstName,
        String lastName,
        TrainingTypeName specialization,
        boolean isActive,
        List<TraineeSummary> trainees
) {
}