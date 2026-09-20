package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer;


import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainee.TraineeSummary;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;

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