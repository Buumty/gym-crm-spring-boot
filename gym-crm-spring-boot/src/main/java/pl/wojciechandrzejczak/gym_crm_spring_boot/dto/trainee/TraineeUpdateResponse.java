package org.example.dto.trainee;

import org.example.dto.trainer.TrainerSummary;

import java.time.LocalDate;
import java.util.List;

public record TraineeUpdateResponse(
        String username,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        String address,
        boolean isActive,
        List<TrainerSummary> trainers
) {
}