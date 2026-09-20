package org.example.dto.trainer;

import org.example.model.TrainingTypeName;

public record TrainerSummary(
        String username,
        String firstName,
        String lastName,
        TrainingTypeName specialization
) {
}