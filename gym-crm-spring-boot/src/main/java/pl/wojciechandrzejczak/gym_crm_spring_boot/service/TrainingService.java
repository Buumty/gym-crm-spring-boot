package pl.wojciechandrzejczak.gym_crm_spring_boot.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TraineeTrainingResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TrainerTrainingResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.*;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TraineeRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainerRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainingRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainingTypeRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication.AuthenticationService;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Validated
@Transactional(readOnly = true)
public class TrainingService {
    private static final Logger log =
            LoggerFactory.getLogger(TrainingService.class);

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final AuthenticationService authenticationService;

    public TrainingService(TraineeRepository traineeRepository, TrainerRepository trainerRepository, TrainingRepository trainingRepository, TrainingTypeRepository trainingTypeRepository, AuthenticationService authenticationService) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.authenticationService = authenticationService;
    }

    public List<Training> getTraineeTrainings(
            @NotBlank String authUsername,
            @NotBlank String authPassword,
            @NotBlank String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            TrainingTypeName trainingType
    ) {
        authenticationService.requireAuthentication(
                authUsername,
                authPassword
        );

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {

            log.warn(
                    "Invalid trainee training date range: fromDate={} toDate={} traineeUsername={}",
                    fromDate,
                    toDate,
                    traineeUsername
            );

            throw new IllegalArgumentException(
                    "From date cannot be after to date"
            );
        }

        log.debug(
                "Searching trainee trainings for username={} fromDate={} toDate={} trainerName={} trainingType={}",
                traineeUsername,
                fromDate,
                toDate,
                trainerName,
                trainingType
        );

        return trainingRepository.findTraineeTrainings(
                traineeUsername,
                fromDate,
                toDate,
                trainerName,
                trainingType
        );
    }
    public List<Training> getTrainerTrainings(
            @NotBlank String authUsername,
            @NotBlank String authPassword,
            @NotBlank String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    ) {
        authenticationService.requireAuthentication(
                authUsername,
                authPassword
        );

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {

            log.warn(
                    "Invalid trainer training date range: fromDate={} toDate={} trainerUsername={}",
                    fromDate,
                    toDate,
                    trainerUsername
            );

            throw new IllegalArgumentException(
                    "From date cannot be after to date"
            );
        }

        log.debug(
                "Searching trainer trainings for username={} fromDate={} toDate={} traineeName={}",
                trainerUsername,
                fromDate,
                toDate,
                traineeName
        );

        return trainingRepository.findTrainerTrainings(
                trainerUsername,
                fromDate,
                toDate,
                traineeName
        );
    }

    @Transactional
    public Training create(
            @NotBlank String authUsername,
            @NotBlank String authPassword,
            @NotBlank String traineeUsername,
            @NotBlank String trainerUsername,
            @NotBlank String trainingName,
            @NotNull TrainingTypeName trainingTypeName,
            @NotNull LocalDate trainingDate,
            @NotNull @Positive Integer trainingDuration
    ) {
        authenticationService.requireAuthentication(
                authUsername,
                authPassword
        );

        Trainee trainee = traineeRepository
                .findByUsername(traineeUsername)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Trainee not found"
                        )
                );

        Trainer trainer = trainerRepository
                .findByUsername(trainerUsername)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Trainer not found"
                        )
                );

        TrainingType trainingType = trainingTypeRepository
                .findByName(trainingTypeName)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Training type not found"
                        )
                );

        Training training = new Training(
                trainee,
                trainer,
                trainingName,
                trainingType,
                trainingDate,
                trainingDuration
        );

        Training savedTraining = trainingRepository.save(training);

        log.info(
                "Created training name={} traineeUsername={} trainerUsername={} type={} date={} duration={}",
                trainingName,
                traineeUsername,
                trainerUsername,
                trainingTypeName,
                trainingDate,
                trainingDuration
        );

        return savedTraining;
    }

    @Transactional
    public void addTraining(
            @NotBlank String authUsername,
            @NotBlank String authPassword,
            @NotBlank String traineeUsername,
            @NotBlank String trainerUsername,
            @NotBlank String trainingName,
            @NotNull LocalDate trainingDate,
            @NotNull @Positive Integer trainingDuration
    ) {
        authenticationService.requireAuthentication(
                authUsername,
                authPassword
        );

        Trainer trainer = trainerRepository.findByUsername(trainerUsername)
                .orElseThrow(() ->
                        new NoSuchElementException("Trainer not found")
                );

        TrainingTypeName trainingType =
                trainer.getSpecialization().getTrainingTypeName();

        create(
                authUsername,
                authPassword,
                traineeUsername,
                trainerUsername,
                trainingName,
                trainingType,
                trainingDate,
                trainingDuration
        );
    }

    public List<TraineeTrainingResponse> getTraineeTrainingList(
            @NotBlank String authUsername,
            @NotBlank String authPassword,
            @NotBlank String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            TrainingTypeName trainingType
    ) {
        return getTraineeTrainings(
                authUsername,
                authPassword,
                traineeUsername,
                fromDate,
                toDate,
                trainerName,
                trainingType
        ).stream()
                .map(training -> new TraineeTrainingResponse(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().getTrainingTypeName(),
                        training.getTrainingDuration(),
                        training.getTrainer().getUser().getFirstName()
                                + " "
                                + training.getTrainer().getUser().getLastName()
                ))
                .toList();
    }

    public List<TrainerTrainingResponse> getTrainerTrainingList(
            @NotBlank String authUsername,
            @NotBlank String authPassword,
            @NotBlank String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    ) {
        return getTrainerTrainings(
                authUsername,
                authPassword,
                trainerUsername,
                fromDate,
                toDate,
                traineeName
        ).stream()
                .map(training -> new TrainerTrainingResponse(
                        training.getTrainingName(),
                        training.getTrainingDate(),
                        training.getTrainingType().getTrainingTypeName(),
                        training.getTrainingDuration(),
                        training.getTrainee().getUser().getFirstName()
                                + " "
                                + training.getTrainee().getUser().getLastName()
                ))
                .toList();
    }
}
