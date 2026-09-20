package pl.wojciechandrzejczak.gym_crm_spring_boot.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private GymFacade gymFacade;

    @Test
    void shouldCreateTrainee() {
        String firstName = "Jan";
        String lastName = "Kowalski";
        LocalDate dateOfBirth = LocalDate.of(2000, 1, 10);
        String address = "Poznan";

        Trainee expected = mock(Trainee.class);

        when(traineeService.create(
                firstName,
                lastName,
                dateOfBirth,
                address
        )).thenReturn(expected);

        Trainee result = gymFacade.createTrainee(
                firstName,
                lastName,
                dateOfBirth,
                address
        );

        assertSame(expected, result);

        verify(traineeService).create(
                firstName,
                lastName,
                dateOfBirth,
                address
        );
    }

    @Test
    void shouldFindTraineeByUsername() {
        String username = "jan.kowalski";
        String password = "password";

        Trainee expected = mock(Trainee.class);

        when(traineeService.findByUsername(
                username,
                password
        )).thenReturn(expected);

        Trainee result = gymFacade.findTraineeByUsername(
                username,
                password
        );

        assertSame(expected, result);

        verify(traineeService).findByUsername(
                username,
                password
        );
    }

    @Test
    void shouldFindTraineeById() {
        long id = 1L;
        String username = "jan.kowalski";
        String password = "password";

        Trainee expected = mock(Trainee.class);

        when(traineeService.findById(
                id,
                username,
                password
        )).thenReturn(expected);

        Trainee result = gymFacade.findTraineeById(
                id,
                username,
                password
        );

        assertSame(expected, result);

        verify(traineeService).findById(
                id,
                username,
                password
        );
    }

    @Test
    void shouldFindAllTrainees() {
        String username = "admin";
        String password = "password";

        List<Trainee> expected = List.of(
                mock(Trainee.class),
                mock(Trainee.class)
        );

        when(traineeService.findAll(
                username,
                password
        )).thenReturn(expected);

        List<Trainee> result = gymFacade.findAllTrainees(
                username,
                password
        );

        assertSame(expected, result);

        verify(traineeService).findAll(
                username,
                password
        );
    }

    @Test
    void shouldUpdateTrainee() {
        String firstName = "Jan";
        String lastName = "Nowak";
        String address = "Warszawa";
        LocalDate dateOfBirth = LocalDate.of(2000, 1, 10);
        String username = "jan.kowalski";
        String password = "password";

        Trainee expected = mock(Trainee.class);

        when(traineeService.update(
                firstName,
                lastName,
                address,
                dateOfBirth,
                username,
                password
        )).thenReturn(expected);

        Trainee result = gymFacade.updateTrainee(
                firstName,
                lastName,
                address,
                dateOfBirth,
                username,
                password
        );

        assertSame(expected, result);

        verify(traineeService).update(
                firstName,
                lastName,
                address,
                dateOfBirth,
                username,
                password
        );
    }

    @Test
    void shouldDeleteTrainee() {
        String username = "jan.kowalski";
        String password = "password";

        gymFacade.deleteTrainee(
                username,
                password
        );

        verify(traineeService).deleteByUsername(
                username,
                password
        );
    }

    @Test
    void shouldChangeTraineePassword() {
        String username = "jan.kowalski";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        gymFacade.changeTraineePassword(
                username,
                oldPassword,
                newPassword
        );

        verify(traineeService).changePassword(
                username,
                oldPassword,
                newPassword
        );
    }

    @Test
    void shouldActivateTrainee() {
        String username = "jan.kowalski";
        String password = "password";

        gymFacade.activateTrainee(
                username,
                password
        );

        verify(traineeService).activate(
                username,
                password
        );
    }

    @Test
    void shouldDeactivateTrainee() {
        String username = "jan.kowalski";
        String password = "password";

        gymFacade.deactivateTrainee(
                username,
                password
        );

        verify(traineeService).deactivate(
                username,
                password
        );
    }

    @Test
    void shouldUpdateTraineeTrainers() {
        String username = "jan.kowalski";
        String password = "password";
        Set<String> trainerUsernames = Set.of(
                "trainer1",
                "trainer2"
        );

        Trainee expected = mock(Trainee.class);

        when(traineeService.updateTrainers(
                username,
                password,
                trainerUsernames
        )).thenReturn(expected);

        Trainee result = gymFacade.updateTraineeTrainers(
                username,
                password,
                trainerUsernames
        );

        assertSame(expected, result);

        verify(traineeService).updateTrainers(
                username,
                password,
                trainerUsernames
        );
    }

    @Test
    void shouldCreateTrainer() {
        String firstName = "Adam";
        String lastName = "Nowak";
        TrainingTypeName specialization =
                TrainingTypeName.values()[0];

        Trainer expected = mock(Trainer.class);

        when(trainerService.create(
                firstName,
                lastName,
                specialization
        )).thenReturn(expected);

        Trainer result = gymFacade.createTrainer(
                firstName,
                lastName,
                specialization
        );

        assertSame(expected, result);

        verify(trainerService).create(
                firstName,
                lastName,
                specialization
        );
    }

    @Test
    void shouldFindTrainerByUsername() {
        String username = "trainer1";
        String password = "password";

        Trainer expected = mock(Trainer.class);

        when(trainerService.findByUsername(
                username,
                password
        )).thenReturn(expected);

        Trainer result = gymFacade.findTrainerByUsername(
                username,
                password
        );

        assertSame(expected, result);

        verify(trainerService).findByUsername(
                username,
                password
        );
    }

    @Test
    void shouldUpdateTrainer() {
        String firstName = "Adam";
        String lastName = "Nowak";
        TrainingTypeName specialization =
                TrainingTypeName.values()[0];
        String username = "trainer1";
        String password = "password";

        Trainer expected = mock(Trainer.class);

        when(trainerService.update(
                firstName,
                lastName,
                specialization,
                username,
                password
        )).thenReturn(expected);

        Trainer result = gymFacade.updateTrainer(
                firstName,
                lastName,
                specialization,
                username,
                password
        );

        assertSame(expected, result);

        verify(trainerService).update(
                firstName,
                lastName,
                specialization,
                username,
                password
        );
    }

    @Test
    void shouldChangeTrainerPassword() {
        String username = "trainer1";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";

        gymFacade.changeTrainerPassword(
                username,
                oldPassword,
                newPassword
        );

        verify(trainerService).changePassword(
                username,
                oldPassword,
                newPassword
        );
    }

    @Test
    void shouldActivateTrainer() {
        String username = "trainer1";
        String password = "password";

        gymFacade.activateTrainer(
                username,
                password
        );

        verify(trainerService).activate(
                username,
                password
        );
    }

    @Test
    void shouldDeactivateTrainer() {
        String username = "trainer1";
        String password = "password";

        gymFacade.deactivateTrainer(
                username,
                password
        );

        verify(trainerService).deactivate(
                username,
                password
        );
    }

    @Test
    void shouldFindTrainersNotAssignedToTrainee() {
        String traineeUsername = "jan.kowalski";
        String password = "password";

        List<Trainer> expected = List.of(
                mock(Trainer.class)
        );

        when(trainerService.findNotAssignedToTrainee(
                traineeUsername,
                password
        )).thenReturn(expected);

        List<Trainer> result =
                gymFacade.findTrainersNotAssignedToTrainee(
                        traineeUsername,
                        password
                );

        assertSame(expected, result);

        verify(trainerService).findNotAssignedToTrainee(
                traineeUsername,
                password
        );
    }

    @Test
    void shouldCreateTraining() {
        String authUsername = "admin";
        String authPassword = "password";
        String traineeUsername = "trainee1";
        String trainerUsername = "trainer1";
        String trainingName = "Morning training";
        TrainingTypeName trainingType =
                TrainingTypeName.values()[0];
        LocalDate trainingDate = LocalDate.of(2026, 9, 18);
        Integer trainingDuration = 60;

        Training expected = mock(Training.class);

        when(trainingService.create(
                authUsername,
                authPassword,
                traineeUsername,
                trainerUsername,
                trainingName,
                trainingType,
                trainingDate,
                trainingDuration
        )).thenReturn(expected);

        Training result = gymFacade.createTraining(
                authUsername,
                authPassword,
                traineeUsername,
                trainerUsername,
                trainingName,
                trainingType,
                trainingDate,
                trainingDuration
        );

        assertSame(expected, result);

        verify(trainingService).create(
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

    @Test
    void shouldGetTraineeTrainings() {
        String authUsername = "admin";
        String authPassword = "password";
        String traineeUsername = "trainee1";
        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 12, 31);
        String trainerName = "Adam";
        TrainingTypeName trainingType =
                TrainingTypeName.values()[0];

        List<Training> expected = List.of(
                mock(Training.class)
        );

        when(trainingService.getTraineeTrainings(
                authUsername,
                authPassword,
                traineeUsername,
                fromDate,
                toDate,
                trainerName,
                trainingType
        )).thenReturn(expected);

        List<Training> result =
                gymFacade.getTraineeTrainings(
                        authUsername,
                        authPassword,
                        traineeUsername,
                        fromDate,
                        toDate,
                        trainerName,
                        trainingType
                );

        assertSame(expected, result);

        verify(trainingService).getTraineeTrainings(
                authUsername,
                authPassword,
                traineeUsername,
                fromDate,
                toDate,
                trainerName,
                trainingType
        );
    }

    @Test
    void shouldGetTrainerTrainings() {
        String authUsername = "admin";
        String authPassword = "password";
        String trainerUsername = "trainer1";
        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 12, 31);
        String traineeName = "Jan";

        List<Training> expected = List.of(
                mock(Training.class)
        );

        when(trainingService.getTrainerTrainings(
                authUsername,
                authPassword,
                trainerUsername,
                fromDate,
                toDate,
                traineeName
        )).thenReturn(expected);

        List<Training> result =
                gymFacade.getTrainerTrainings(
                        authUsername,
                        authPassword,
                        trainerUsername,
                        fromDate,
                        toDate,
                        traineeName
                );

        assertSame(expected, result);

        verify(trainingService).getTrainerTrainings(
                authUsername,
                authPassword,
                trainerUsername,
                fromDate,
                toDate,
                traineeName
        );
    }

    @Test
    void shouldGetTraineeProfile() {
        String username = "trainee1";
        String password = "password";

        TraineeProfileResponse expected =
                mock(TraineeProfileResponse.class);

        when(traineeService.getProfile(
                username,
                password
        )).thenReturn(expected);

        TraineeProfileResponse result =
                gymFacade.getTraineeProfile(
                        username,
                        password
                );

        assertSame(expected, result);

        verify(traineeService).getProfile(
                username,
                password
        );
    }

    @Test
    void shouldGetTrainerProfile() {
        String username = "trainer1";
        String password = "password";

        TrainerProfileResponse expected =
                mock(TrainerProfileResponse.class);

        when(trainerService.getProfile(
                username,
                password
        )).thenReturn(expected);

        TrainerProfileResponse result =
                gymFacade.getTrainerProfile(
                        username,
                        password
                );

        assertSame(expected, result);

        verify(trainerService).getProfile(
                username,
                password
        );
    }

    @Test
    void shouldUpdateTraineeProfile() {
        String username = "trainee1";
        String password = "password";
        String firstName = "Jan";
        String lastName = "Nowak";
        LocalDate dateOfBirth = LocalDate.of(2000, 1, 1);
        String address = "Poznan";
        Boolean isActive = true;

        TraineeUpdateResponse expected =
                mock(TraineeUpdateResponse.class);

        when(traineeService.updateProfile(
                username,
                password,
                firstName,
                lastName,
                dateOfBirth,
                address,
                isActive
        )).thenReturn(expected);

        TraineeUpdateResponse result =
                gymFacade.updateTraineeProfile(
                        username,
                        password,
                        firstName,
                        lastName,
                        dateOfBirth,
                        address,
                        isActive
                );

        assertSame(expected, result);

        verify(traineeService).updateProfile(
                username,
                password,
                firstName,
                lastName,
                dateOfBirth,
                address,
                isActive
        );
    }

    @Test
    void shouldUpdateTrainerProfile() {
        String username = "trainer1";
        String password = "password";
        String firstName = "Adam";
        String lastName = "Nowak";
        Boolean isActive = true;

        TrainerUpdateResponse expected =
                mock(TrainerUpdateResponse.class);

        when(trainerService.updateProfile(
                username,
                password,
                firstName,
                lastName,
                isActive
        )).thenReturn(expected);

        TrainerUpdateResponse result =
                gymFacade.updateTrainerProfile(
                        username,
                        password,
                        firstName,
                        lastName,
                        isActive
                );

        assertSame(expected, result);

        verify(trainerService).updateProfile(
                username,
                password,
                firstName,
                lastName,
                isActive
        );
    }

    @Test
    void shouldGetUnassignedTrainers() {
        String traineeUsername = "trainee1";
        String password = "password";

        List<TrainerSummary> expected = List.of(
                mock(TrainerSummary.class)
        );

        when(trainerService.getUnassignedTrainers(
                traineeUsername,
                password
        )).thenReturn(expected);

        List<TrainerSummary> result =
                gymFacade.getUnassignedTrainers(
                        traineeUsername,
                        password
                );

        assertSame(expected, result);

        verify(trainerService).getUnassignedTrainers(
                traineeUsername,
                password
        );
    }

    @Test
    void shouldUpdateTraineeTrainersList() {
        String username = "trainee1";
        String password = "password";
        Set<String> trainerUsernames = Set.of(
                "trainer1",
                "trainer2"
        );

        List<TrainerSummary> expected = List.of(
                mock(TrainerSummary.class)
        );

        when(traineeService.updateTrainersList(
                username,
                password,
                trainerUsernames
        )).thenReturn(expected);

        List<TrainerSummary> result =
                gymFacade.updateTraineeTrainersList(
                        username,
                        password,
                        trainerUsernames
                );

        assertSame(expected, result);

        verify(traineeService).updateTrainersList(
                username,
                password,
                trainerUsernames
        );
    }

    @Test
    void shouldGetTrainingTypes() {
        String username = "user";
        String password = "password";

        List<TrainingTypeResponse> expected = List.of(
                mock(TrainingTypeResponse.class)
        );

        when(trainingTypeService.findAll(
                username,
                password
        )).thenReturn(expected);

        List<TrainingTypeResponse> result =
                gymFacade.getTrainingTypes(
                        username,
                        password
                );

        assertSame(expected, result);

        verify(trainingTypeService).findAll(
                username,
                password
        );
    }

    @Test
    void shouldAddTraining() {
        String authUsername = "admin";
        String authPassword = "password";
        String traineeUsername = "trainee1";
        String trainerUsername = "trainer1";
        String trainingName = "Morning training";
        LocalDate trainingDate = LocalDate.of(2026, 9, 18);
        Integer trainingDuration = 60;

        gymFacade.addTraining(
                authUsername,
                authPassword,
                traineeUsername,
                trainerUsername,
                trainingName,
                trainingDate,
                trainingDuration
        );

        verify(trainingService).addTraining(
                authUsername,
                authPassword,
                traineeUsername,
                trainerUsername,
                trainingName,
                trainingDate,
                trainingDuration
        );
    }

    @Test
    void shouldGetTraineeTrainingList() {
        String authUsername = "admin";
        String authPassword = "password";
        String traineeUsername = "trainee1";
        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 12, 31);
        String trainerName = "Adam";
        TrainingTypeName trainingType =
                TrainingTypeName.values()[0];

        List<TraineeTrainingResponse> expected = List.of(
                mock(TraineeTrainingResponse.class)
        );

        when(trainingService.getTraineeTrainingList(
                authUsername,
                authPassword,
                traineeUsername,
                fromDate,
                toDate,
                trainerName,
                trainingType
        )).thenReturn(expected);

        List<TraineeTrainingResponse> result =
                gymFacade.getTraineeTrainingList(
                        authUsername,
                        authPassword,
                        traineeUsername,
                        fromDate,
                        toDate,
                        trainerName,
                        trainingType
                );

        assertSame(expected, result);

        verify(trainingService).getTraineeTrainingList(
                authUsername,
                authPassword,
                traineeUsername,
                fromDate,
                toDate,
                trainerName,
                trainingType
        );
    }

    @Test
    void shouldGetTrainerTrainingList() {
        String authUsername = "admin";
        String authPassword = "password";
        String trainerUsername = "trainer1";
        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 12, 31);
        String traineeName = "Jan";

        List<TrainerTrainingResponse> expected = List.of(
                mock(TrainerTrainingResponse.class)
        );

        when(trainingService.getTrainerTrainingList(
                authUsername,
                authPassword,
                trainerUsername,
                fromDate,
                toDate,
                traineeName
        )).thenReturn(expected);

        List<TrainerTrainingResponse> result =
                gymFacade.getTrainerTrainingList(
                        authUsername,
                        authPassword,
                        trainerUsername,
                        fromDate,
                        toDate,
                        traineeName
                );

        assertSame(expected, result);

        verify(trainingService).getTrainerTrainingList(
                authUsername,
                authPassword,
                trainerUsername,
                fromDate,
                toDate,
                traineeName
        );
    }
}