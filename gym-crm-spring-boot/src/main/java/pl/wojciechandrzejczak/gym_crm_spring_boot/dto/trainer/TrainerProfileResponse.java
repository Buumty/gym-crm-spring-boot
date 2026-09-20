package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer;

import org.example.dto.trainee.TraineeSummary;
import org.example.model.TrainingTypeName;

import java.util.List;

public record TrainerProfileResponse(
        String firstName,
        String lastName,
        TrainingTypeName specialization,
        boolean isActive,
        List<TraineeSummary> trainees
) {
}