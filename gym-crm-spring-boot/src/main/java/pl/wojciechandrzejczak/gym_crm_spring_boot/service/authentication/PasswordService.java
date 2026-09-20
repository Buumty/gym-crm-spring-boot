package org.example.service.authentication;

import jakarta.validation.constraints.NotBlank;
import org.example.service.TraineeService;
import org.example.service.TrainerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
public class PasswordService {

    private final AuthenticationService authenticationService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public PasswordService(
            AuthenticationService authenticationService,
            TraineeService traineeService,
            TrainerService trainerService
    ) {
        this.authenticationService = authenticationService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @Transactional
    public void changePassword(
            @NotBlank String username,
            @NotBlank String oldPassword,
            @NotBlank String newPassword
    ) {
        if (authenticationService.traineeCredentialsValidation(
                username, oldPassword
        )) {
            traineeService.changePassword(
                    username, oldPassword, newPassword
            );
        } else if (authenticationService.trainerCredentialsValidation(
                username, oldPassword
        )) {
            trainerService.changePassword(
                    username, oldPassword, newPassword
            );
        } else {
            throw new SecurityException("Invalid credentials");
        }
    }
}