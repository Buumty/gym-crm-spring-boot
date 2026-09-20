package pl.wojciechandrzejczak.gym_crm_spring_boot.facade;

import org.springframework.stereotype.Component;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainee.TraineeProfileResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainee.TraineeUpdateResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerProfileResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerSummary;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerUpdateResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TraineeTrainingResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TrainerTrainingResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TrainingTypeResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainee;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainer;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Training;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.TraineeService;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.TrainerService;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.TrainingService;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.TrainingTypeService;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Component
public class GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    public GymFacade(
            TraineeService traineeService,
            TrainerService trainerService,
            TrainingService trainingService,
            TrainingTypeService trainingTypeService
    ) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
    }

    public Trainee createTrainee(
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address
    ) {
        return traineeService.create(
                firstName,
                lastName,
                dateOfBirth,
                address
        );
    }

    public Trainee findTraineeByUsername(
            String username,
            String password
    ) {
        return traineeService.findByUsername(
                username,
                password
        );
    }

    public Trainee findTraineeById(
            long id,
            String username,
            String password
    ) {
        return traineeService.findById(
                id,
                username,
                password
        );
    }

    public List<Trainee> findAllTrainees(
            String username,
            String password
    ) {
        return traineeService.findAll(
                username,
                password
        );
    }

    public Trainee updateTrainee(
            String firstName,
            String lastName,
            String address,
            LocalDate dateOfBirth,
            String username,
            String password
    ) {
        return traineeService.update(
                firstName,
                lastName,
                address,
                dateOfBirth,
                username,
                password
        );
    }

    public void deleteTrainee(
            String username,
            String password
    ) {
        traineeService.deleteByUsername(
                username,
                password
        );
    }

    public void changeTraineePassword(
            String username,
            String oldPassword,
            String newPassword
    ) {
        traineeService.changePassword(
                username,
                oldPassword,
                newPassword
        );
    }

    public void activateTrainee(
            String username,
            String password
    ) {
        traineeService.activate(
                username,
                password
        );
    }

    public void deactivateTrainee(
            String username,
            String password
    ) {
        traineeService.deactivate(
                username,
                password
        );
    }

    public Trainee updateTraineeTrainers(
            String username,
            String password,
            Set<String> trainerUsernames
    ) {
        return traineeService.updateTrainers(
                username,
                password,
                trainerUsernames
        );
    }

    public Trainer createTrainer(
            String firstName,
            String lastName,
            TrainingTypeName specialization
    ) {
        return trainerService.create(
                firstName,
                lastName,
                specialization
        );
    }

    public Trainer findTrainerByUsername(
            String username,
            String password
    ) {
        return trainerService.findByUsername(
                username,
                password
        );
    }

    public Trainer updateTrainer(
            String firstName,
            String lastName,
            TrainingTypeName specialization,
            String username,
            String password
    ) {
        return trainerService.update(
                firstName,
                lastName,
                specialization,
                username,
                password
        );
    }

    public void changeTrainerPassword(
            String username,
            String oldPassword,
            String newPassword
    ) {
        trainerService.changePassword(
                username,
                oldPassword,
                newPassword
        );
    }

    public void activateTrainer(
            String username,
            String password
    ) {
        trainerService.activate(
                username,
                password
        );
    }

    public void deactivateTrainer(
            String username,
            String password
    ) {
        trainerService.deactivate(
                username,
                password
        );
    }

    public List<Trainer> findTrainersNotAssignedToTrainee(
            String traineeUsername,
            String password
    ) {
        return trainerService.findNotAssignedToTrainee(
                traineeUsername,
                password
        );
    }


    public Training createTraining(
            String authUsername,
            String authPassword,
            String traineeUsername,
            String trainerUsername,
            String trainingName,
            TrainingTypeName trainingType,
            LocalDate trainingDate,
            Integer trainingDuration
    ) {
        return trainingService.create(
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

    public List<Training> getTraineeTrainings(
            String authUsername,
            String authPassword,
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            TrainingTypeName trainingType
    ) {
        return trainingService.getTraineeTrainings(
                authUsername,
                authPassword,
                traineeUsername,
                fromDate,
                toDate,
                trainerName,
                trainingType
        );
    }

    public List<Training> getTrainerTrainings(
            String authUsername,
            String authPassword,
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    ) {
        return trainingService.getTrainerTrainings(
                authUsername,
                authPassword,
                trainerUsername,
                fromDate,
                toDate,
                traineeName
        );
    }

    public TraineeProfileResponse getTraineeProfile(
            String username,
            String password
    ) {
        return traineeService.getProfile(username, password);
    }
    public TrainerProfileResponse getTrainerProfile(
            String username,
            String password
    ) {
        return trainerService.getProfile(username, password);
    }

    public TraineeUpdateResponse updateTraineeProfile(
            String username,
            String password,
            String firstName,
            String lastName,
            LocalDate dateOfBirth,
            String address,
            Boolean isActive
    ) {
        return traineeService.updateProfile(
                username,
                password,
                firstName,
                lastName,
                dateOfBirth,
                address,
                isActive
        );
    }

    public TrainerUpdateResponse updateTrainerProfile(
            String username,
            String password,
            String firstName,
            String lastName,
            Boolean isActive
    ) {
        return trainerService.updateProfile(
                username,
                password,
                firstName,
                lastName,
                isActive
        );
    }

    public List<TrainerSummary> getUnassignedTrainers(
            String traineeUsername,
            String password
    ) {
        return trainerService.getUnassignedTrainers(
                traineeUsername,
                password
        );
    }

    public List<TrainerSummary> updateTraineeTrainersList(
            String username,
            String password,
            Set<String> trainerUsernames
    ) {
        return traineeService.updateTrainersList(
                username,
                password,
                trainerUsernames
        );
    }

    public List<TrainingTypeResponse> getTrainingTypes(
            String username,
            String password
    ) {
        return trainingTypeService.findAll(username, password);
    }

    public void addTraining(
            String authUsername,
            String authPassword,
            String traineeUsername,
            String trainerUsername,
            String trainingName,
            LocalDate trainingDate,
            Integer trainingDuration
    ) {
        trainingService.addTraining(
                authUsername,
                authPassword,
                traineeUsername,
                trainerUsername,
                trainingName,
                trainingDate,
                trainingDuration
        );
    }

    public List<TraineeTrainingResponse> getTraineeTrainingList(
            String authUsername,
            String authPassword,
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            TrainingTypeName trainingType
    ) {
        return trainingService.getTraineeTrainingList(
                authUsername,
                authPassword,
                traineeUsername,
                fromDate,
                toDate,
                trainerName,
                trainingType
        );
    }

    public List<TrainerTrainingResponse> getTrainerTrainingList(
            String authUsername,
            String authPassword,
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    ) {
        return trainingService.getTrainerTrainingList(
                authUsername,
                authPassword,
                trainerUsername,
                fromDate,
                toDate,
                traineeName
        );
    }
}