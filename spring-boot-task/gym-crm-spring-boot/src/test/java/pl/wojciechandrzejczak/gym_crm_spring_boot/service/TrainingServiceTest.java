package pl.wojciechandrzejczak.gym_crm_spring_boot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TraineeTrainingResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TrainerTrainingResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.metrics.GymMetrics;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.*;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TraineeRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainerRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainingRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainingTypeRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication.AuthenticationService;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private GymMetrics gymMetrics;

    @Mock
    private TrainingRepository trainingRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private TrainingService trainingService;

    @Test
    void shouldCreateTraining() {
        Trainee trainee = createTrainee();
        TrainingType trainingType =
                new TrainingType(TrainingTypeName.STRENGTH);
        Trainer trainer = createTrainer(trainingType);

        LocalDate trainingDate =
                LocalDate.of(2026, 8, 10);

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUser_Username(
                "Anna.Brown"
        )).thenReturn(Optional.of(trainer));

        when(trainingTypeRepository.findByTrainingTypeName(
                TrainingTypeName.STRENGTH
        )).thenReturn(Optional.of(trainingType));

        when(trainingRepository.save(any(Training.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Training result = trainingService.create(
                "John.Smith",
                "password",
                "John.Smith",
                "Anna.Brown",
                "Strength training",
                TrainingTypeName.STRENGTH,
                trainingDate,
                60
        );

        assertSame(
                trainee,
                result.getTrainee()
        );

        assertSame(
                trainer,
                result.getTrainer()
        );

        assertSame(
                trainingType,
                result.getTrainingType()
        );

        assertEquals(
                "Strength training",
                result.getTrainingName()
        );

        assertEquals(
                trainingDate,
                result.getTrainingDate()
        );

        assertEquals(
                60,
                result.getTrainingDuration()
        );

        verify(gymMetrics).incrementTrainingsCreated();

        verify(authenticationService)
                .requireAuthentication(
                        "John.Smith",
                        "password"
                );

        verify(traineeRepository)
                .findByUser_Username("John.Smith");

        verify(trainerRepository)
                .findByUser_Username("Anna.Brown");

        verify(trainingTypeRepository)
                .findByTrainingTypeName(
                        TrainingTypeName.STRENGTH
                );

        verify(trainingRepository)
                .save(any(Training.class));
    }

    @Test
    void shouldThrowExceptionWhenTraineeDoesNotExistDuringCreate() {
        when(traineeRepository.findByUser_Username(
                "Unknown.Trainee"
        )).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> trainingService.create(
                        "John.Smith",
                        "password",
                        "Unknown.Trainee",
                        "Anna.Brown",
                        "Strength training",
                        TrainingTypeName.STRENGTH,
                        LocalDate.of(2026, 8, 10),
                        60
                )
        );

        verify(authenticationService)
                .requireAuthentication(
                        "John.Smith",
                        "password"
                );

        verify(trainerRepository, never())
                .findByUser_Username(anyString());

        verify(trainingRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenTrainerDoesNotExistDuringCreate() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUser_Username(
                "Unknown.Trainer"
        )).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> trainingService.create(
                        "John.Smith",
                        "password",
                        "John.Smith",
                        "Unknown.Trainer",
                        "Strength training",
                        TrainingTypeName.STRENGTH,
                        LocalDate.of(2026, 8, 10),
                        60
                )
        );

        verify(trainingTypeRepository, never())
                .findByTrainingTypeName(any());

        verify(trainingRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenTrainingTypeDoesNotExistDuringCreate() {
        Trainee trainee = createTrainee();

        TrainingType trainerSpecialization =
                new TrainingType(
                        TrainingTypeName.STRENGTH
                );

        Trainer trainer =
                createTrainer(trainerSpecialization);

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUser_Username(
                "Anna.Brown"
        )).thenReturn(Optional.of(trainer));

        when(trainingTypeRepository.findByTrainingTypeName(
                TrainingTypeName.STRENGTH
        )).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> trainingService.create(
                        "John.Smith",
                        "password",
                        "John.Smith",
                        "Anna.Brown",
                        "Strength training",
                        TrainingTypeName.STRENGTH,
                        LocalDate.of(2026, 8, 10),
                        60
                )
        );

        verify(trainingRepository, never())
                .save(any());
    }

    @Test
    void shouldReturnTraineeTrainings() {
        Training training = createTraining();

        LocalDate fromDate =
                LocalDate.of(2026, 8, 1);

        LocalDate toDate =
                LocalDate.of(2026, 8, 31);

        when(trainingRepository.findTraineeTrainings(
                "John.Smith",
                fromDate,
                toDate,
                "Anna Brown",
                TrainingTypeName.STRENGTH
        )).thenReturn(List.of(training));

        List<Training> result =
                trainingService.getTraineeTrainings(
                        "John.Smith",
                        "password",
                        "John.Smith",
                        fromDate,
                        toDate,
                        "Anna Brown",
                        TrainingTypeName.STRENGTH
                );

        assertEquals(1, result.size());
        assertSame(training, result.getFirst());

        verify(authenticationService)
                .requireAuthentication(
                        "John.Smith",
                        "password"
                );

        verify(trainingRepository)
                .findTraineeTrainings(
                        "John.Smith",
                        fromDate,
                        toDate,
                        "Anna Brown",
                        TrainingTypeName.STRENGTH
                );
    }

    @Test
    void shouldThrowExceptionWhenTraineeTrainingDateRangeIsInvalid() {
        LocalDate fromDate =
                LocalDate.of(2026, 8, 31);

        LocalDate toDate =
                LocalDate.of(2026, 8, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> trainingService.getTraineeTrainings(
                        "John.Smith",
                        "password",
                        "John.Smith",
                        fromDate,
                        toDate,
                        null,
                        null
                )
        );

        verify(authenticationService)
                .requireAuthentication(
                        "John.Smith",
                        "password"
                );

        verify(trainingRepository, never())
                .findTraineeTrainings(
                        anyString(),
                        any(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldReturnTrainerTrainings() {
        Training training = createTraining();

        LocalDate fromDate =
                LocalDate.of(2026, 8, 1);

        LocalDate toDate =
                LocalDate.of(2026, 8, 31);

        when(trainingRepository.findTrainerTrainings(
                "Anna.Brown",
                fromDate,
                toDate,
                "John Smith"
        )).thenReturn(List.of(training));

        List<Training> result =
                trainingService.getTrainerTrainings(
                        "Anna.Brown",
                        "password",
                        "Anna.Brown",
                        fromDate,
                        toDate,
                        "John Smith"
                );

        assertEquals(1, result.size());
        assertSame(training, result.getFirst());

        verify(authenticationService)
                .requireAuthentication(
                        "Anna.Brown",
                        "password"
                );

        verify(trainingRepository)
                .findTrainerTrainings(
                        "Anna.Brown",
                        fromDate,
                        toDate,
                        "John Smith"
                );
    }

    @Test
    void shouldThrowExceptionWhenTrainerTrainingDateRangeIsInvalid() {
        LocalDate fromDate =
                LocalDate.of(2026, 8, 31);

        LocalDate toDate =
                LocalDate.of(2026, 8, 1);

        assertThrows(
                IllegalArgumentException.class,
                () -> trainingService.getTrainerTrainings(
                        "Anna.Brown",
                        "password",
                        "Anna.Brown",
                        fromDate,
                        toDate,
                        null
                )
        );

        verify(authenticationService)
                .requireAuthentication(
                        "Anna.Brown",
                        "password"
                );

        verify(trainingRepository, never())
                .findTrainerTrainings(
                        anyString(),
                        any(),
                        any(),
                        any()
                );
    }

    @Test
    void shouldAddTrainingUsingTrainerSpecialization() {
        Trainee trainee = createTrainee();

        TrainingType trainingType =
                new TrainingType(
                        TrainingTypeName.STRENGTH
                );

        Trainer trainer =
                createTrainer(trainingType);

        LocalDate trainingDate =
                LocalDate.of(2026, 8, 10);

        when(trainerRepository.findByUser_Username(
                "Anna.Brown"
        )).thenReturn(Optional.of(trainer));

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        when(trainingTypeRepository.findByTrainingTypeName(
                TrainingTypeName.STRENGTH
        )).thenReturn(Optional.of(trainingType));

        when(trainingRepository.save(any(Training.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        trainingService.addTraining(
                "John.Smith",
                "password",
                "John.Smith",
                "Anna.Brown",
                "Strength training",
                trainingDate,
                60
        );

        verify(trainingTypeRepository)
                .findByTrainingTypeName(
                        TrainingTypeName.STRENGTH
                );

        verify(trainingRepository)
                .save(argThat(training ->
                        training.getTrainee() == trainee
                                && training.getTrainer() == trainer
                                && training.getTrainingType() == trainingType
                                && training.getTrainingName()
                                .equals("Strength training")
                                && training.getTrainingDate()
                                .equals(trainingDate)
                                && training.getTrainingDuration()
                                .equals(60)
                ));
    }

    @Test
    void shouldThrowExceptionWhenTrainerDoesNotExistDuringAddTraining() {
        when(trainerRepository.findByUser_Username(
                "Unknown.Trainer"
        )).thenReturn(Optional.empty());

        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () -> trainingService.addTraining(
                                "John.Smith",
                                "password",
                                "John.Smith",
                                "Unknown.Trainer",
                                "Strength training",
                                LocalDate.of(
                                        2026,
                                        8,
                                        10
                                ),
                                60
                        )
                );

        assertEquals(
                "Trainer not found",
                exception.getMessage()
        );

        verify(authenticationService)
                .requireAuthentication(
                        "John.Smith",
                        "password"
                );

        verify(traineeRepository, never())
                .findByUser_Username(anyString());

        verify(trainingTypeRepository, never())
                .findByTrainingTypeName(any());

        verify(trainingRepository, never())
                .save(any());
    }

    @Test
    void shouldReturnTraineeTrainingList() {
        Training training = createTraining();

        LocalDate fromDate =
                LocalDate.of(2026, 8, 1);

        LocalDate toDate =
                LocalDate.of(2026, 8, 31);

        when(trainingRepository.findTraineeTrainings(
                "John.Smith",
                fromDate,
                toDate,
                "Anna Brown",
                TrainingTypeName.STRENGTH
        )).thenReturn(List.of(training));

        List<TraineeTrainingResponse> result =
                trainingService.getTraineeTrainingList(
                        "John.Smith",
                        "password",
                        "John.Smith",
                        fromDate,
                        toDate,
                        "Anna Brown",
                        TrainingTypeName.STRENGTH
                );

        assertEquals(1, result.size());

        TraineeTrainingResponse expected =
                new TraineeTrainingResponse(
                        "Strength training",
                        LocalDate.of(
                                2026,
                                8,
                                10
                        ),
                        TrainingTypeName.STRENGTH,
                        60,
                        "Anna Brown"
                );

        assertEquals(
                expected,
                result.get(0)
        );

        verify(authenticationService)
                .requireAuthentication(
                        "John.Smith",
                        "password"
                );

        verify(trainingRepository)
                .findTraineeTrainings(
                        "John.Smith",
                        fromDate,
                        toDate,
                        "Anna Brown",
                        TrainingTypeName.STRENGTH
                );
    }

    @Test
    void shouldReturnTrainerTrainingList() {
        Training training = createTraining();

        LocalDate fromDate =
                LocalDate.of(2026, 8, 1);

        LocalDate toDate =
                LocalDate.of(2026, 8, 31);

        when(trainingRepository.findTrainerTrainings(
                "Anna.Brown",
                fromDate,
                toDate,
                "John Smith"
        )).thenReturn(List.of(training));

        List<TrainerTrainingResponse> result =
                trainingService.getTrainerTrainingList(
                        "Anna.Brown",
                        "password",
                        "Anna.Brown",
                        fromDate,
                        toDate,
                        "John Smith"
                );

        assertEquals(1, result.size());

        TrainerTrainingResponse expected =
                new TrainerTrainingResponse(
                        "Strength training",
                        LocalDate.of(
                                2026,
                                8,
                                10
                        ),
                        TrainingTypeName.STRENGTH,
                        60,
                        "John Smith"
                );

        assertEquals(
                expected,
                result.get(0)
        );

        verify(authenticationService)
                .requireAuthentication(
                        "Anna.Brown",
                        "password"
                );

        verify(trainingRepository)
                .findTrainerTrainings(
                        "Anna.Brown",
                        fromDate,
                        toDate,
                        "John Smith"
                );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    void shouldIgnoreBlankTrainerName(String trainerName) {
        trainingService.getTraineeTrainings(
                "John.Smith",
                "password",
                "John.Smith",
                null,
                null,
                trainerName,
                null
        );

        verify(trainingRepository).findTraineeTrainings(
                "John.Smith",
                null,
                null,
                null,
                null
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   ", "\t"})
    void shouldIgnoreBlankTraineeName(String traineeName) {
        trainingService.getTrainerTrainings(
                "Anna.Brown",
                "password",
                "Anna.Brown",
                null,
                null,
                traineeName
        );

        verify(trainingRepository).findTrainerTrainings(
                "Anna.Brown",
                null,
                null,
                null
        );
    }

    private Training createTraining() {
        return createTraining(
                "Strength training",
                TrainingTypeName.STRENGTH,
                LocalDate.of(2026, 8, 10),
                60
        );
    }

    private Training createTraining(
            String trainingName,
            TrainingTypeName typeName,
            LocalDate date,
            Integer duration
    ) {
        Trainee trainee = createTrainee();

        TrainingType trainingType =
                new TrainingType(typeName);

        Trainer trainer =
                createTrainer(trainingType);

        return new Training(
                trainee,
                trainer,
                trainingName,
                trainingType,
                date,
                duration
        );
    }

    private Trainee createTrainee() {
        User user = new User(
                "John",
                "Smith",
                "John.Smith",
                "Abc123xyZ9",
                true
        );

        return new Trainee(
                user,
                LocalDate.of(1995, 5, 10),
                "Example address"
        );
    }

    private Trainer createTrainer(
            TrainingType trainingType
    ) {
        User user = new User(
                "Anna",
                "Brown",
                "Anna.Brown",
                "Trainer123",
                true
        );

        return new Trainer(
                trainingType,
                user
        );
    }
}