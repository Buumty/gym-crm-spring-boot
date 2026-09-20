package pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainee;

import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerSummary;

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