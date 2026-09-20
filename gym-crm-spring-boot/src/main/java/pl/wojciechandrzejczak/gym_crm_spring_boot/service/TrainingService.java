package org.example.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.example.dao.TrainingDao;
import org.example.dao.TrainingTypeDao;
import org.example.dto.training.TraineeTrainingResponse;
import org.example.dto.training.TrainerTrainingResponse;
import org.example.model.*;
import org.example.service.authentication.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Validated
@Transactional(readOnly = true)
public class TrainingService {
    private static final Logger log =
            LoggerFactory.getLogger(TrainingService.class);

    private final TrainingDao trainingDao;
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final AuthenticationService authenticationService;

    public TrainingService(TrainingDao trainingDao, TraineeDao traineeDao, TrainerDao trainerDao, TrainingTypeDao trainingTypeDao, AuthenticationService authenticationService) {
        this.trainingDao = trainingDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
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

        return trainingDao.findTraineeTrainings(
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

        return trainingDao.findTrainerTrainings(
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

        Trainee trainee = traineeDao
                .findByUsername(traineeUsername)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Trainee not found"
                        )
                );

        Trainer trainer = trainerDao
                .findByUsername(trainerUsername)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Trainer not found"
                        )
                );

        TrainingType trainingType = trainingTypeDao
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

        Training savedTraining = trainingDao.save(training);

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

        Trainer trainer = trainerDao.findByUsername(trainerUsername)
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
