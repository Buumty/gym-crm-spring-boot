package pl.wojciechandrzejczak.gym_crm_spring_boot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerProfileResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerSummary;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerUpdateResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.metrics.GymMetrics;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.*;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainerRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainingTypeRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication.AuthenticationService;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.generator.PasswordGenerator;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.generator.UsernameGenerator;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private GymMetrics gymMetrics;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private TrainerService trainerService;

    @Test
    void shouldCreateTrainer() {
        TrainingType trainingType =
                new TrainingType(
                        TrainingTypeName.STRENGTH
                );

        when(usernameGenerator.generate(
                "John",
                "Smith"
        )).thenReturn("John.Smith");

        when(passwordGenerator.generate())
                .thenReturn("Abc123xyZ9");

        when(trainingTypeRepository.findByTrainingTypeName(
                TrainingTypeName.STRENGTH
        )).thenReturn(Optional.of(trainingType));

        when(trainerRepository.save(any(Trainer.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Trainer result = trainerService.create(
                "John",
                "Smith",
                TrainingTypeName.STRENGTH
        );

        assertEquals(
                "John",
                result.getUser().getFirstName()
        );

        assertEquals(
                "Smith",
                result.getUser().getLastName()
        );

        assertEquals(
                "John.Smith",
                result.getUser().getUsername()
        );

        assertEquals(
                "Abc123xyZ9",
                result.getUser().getPassword()
        );

        assertTrue(
                result.getUser().isActive()
        );

        assertSame(
                trainingType,
                result.getSpecialization()
        );

        verify(gymMetrics).incrementTrainerRegistrations();

        verify(usernameGenerator)
                .generate("John", "Smith");

        verify(passwordGenerator)
                .generate();

        verify(trainingTypeRepository)
                .findByTrainingTypeName(
                        TrainingTypeName.STRENGTH
                );

        verify(trainerRepository)
                .save(any(Trainer.class));
    }

    @Test
    void shouldThrowExceptionWhenTrainingTypeDoesNotExistDuringCreate() {
        when(usernameGenerator.generate(
                "John",
                "Smith"
        )).thenReturn("John.Smith");

        when(passwordGenerator.generate())
                .thenReturn("Abc123xyZ9");

        when(trainingTypeRepository.findByTrainingTypeName(
                TrainingTypeName.STRENGTH
        )).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> trainerService.create(
                        "John",
                        "Smith",
                        TrainingTypeName.STRENGTH
                )
        );

        verify(trainerRepository, never())
                .save(any());
    }

    @Test
    void shouldUpdateTrainer() {
        Trainer trainer = createTrainer();

        TrainingType newTrainingType =
                new TrainingType(
                        TrainingTypeName.CARDIO
                );

        when(trainerRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainer));

        when(trainingTypeRepository.findByTrainingTypeName(
                TrainingTypeName.CARDIO
        )).thenReturn(
                Optional.of(newTrainingType)
        );

        Trainer result = trainerService.update(
                "Jonathan",
                "Johnson",
                TrainingTypeName.CARDIO,
                "John.Smith",
                "Abc123xyZ9"
        );

        assertSame(trainer, result);

        assertEquals(
                "Jonathan",
                result.getUser().getFirstName()
        );

        assertEquals(
                "Johnson",
                result.getUser().getLastName()
        );

        assertSame(
                newTrainingType,
                result.getSpecialization()
        );

        assertEquals(
                "John.Smith",
                result.getUser().getUsername()
        );

        assertEquals(
                "Abc123xyZ9",
                result.getUser().getPassword()
        );

        verify(authenticationService)
                .requireTrainerAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        verify(trainerRepository)
                .findByUser_Username("John.Smith");

        verify(trainingTypeRepository)
                .findByTrainingTypeName(
                        TrainingTypeName.CARDIO
                );
    }

    @Test
    void shouldFindTrainerByUsername() {
        Trainer trainer = createTrainer();

        when(trainerRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainer));

        Trainer result =
                trainerService.findByUsername(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        assertSame(trainer, result);

        verify(authenticationService)
                .requireTrainerAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        verify(trainerRepository)
                .findByUser_Username("John.Smith");
    }

    @Test
    void shouldChangePassword() {
        Trainer trainer = createTrainer();

        when(trainerRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainer));

        trainerService.changePassword(
                "John.Smith",
                "Abc123xyZ9",
                "NewPassword1"
        );

        assertEquals(
                "NewPassword1",
                trainer.getUser().getPassword()
        );

        verify(authenticationService)
                .requireTrainerAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

    }

    @Test
    void shouldActivateInactiveTrainer() {
        Trainer trainer = createTrainer(
                "John",
                "Smith",
                "John.Smith",
                TrainingTypeName.STRENGTH,
                false
        );

        when(trainerRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainer));

        trainerService.activate(
                "John.Smith",
                "Abc123xyZ9"
        );

        assertTrue(
                trainer.getUser().isActive()
        );
    }

    @Test
    void shouldThrowExceptionWhenActivatingAlreadyActiveTrainer() {
        Trainer trainer = createTrainer();

        when(trainerRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainer));

        assertThrows(
                IllegalStateException.class,
                () -> trainerService.activate(
                        "John.Smith",
                        "Abc123xyZ9"
                )
        );
    }

    @Test
    void shouldDeactivateActiveTrainer() {
        Trainer trainer = createTrainer();

        when(trainerRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainer));

        trainerService.deactivate(
                "John.Smith",
                "Abc123xyZ9"
        );

        assertFalse(
                trainer.getUser().isActive()
        );
    }

    @Test
    void shouldThrowExceptionWhenDeactivatingAlreadyInactiveTrainer() {
        Trainer trainer = createTrainer(
                "John",
                "Smith",
                "John.Smith",
                TrainingTypeName.STRENGTH,
                false
        );

        when(trainerRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainer));

        assertThrows(
                IllegalStateException.class,
                () -> trainerService.deactivate(
                        "John.Smith",
                        "Abc123xyZ9"
                )
        );
    }

    @Test
    void shouldFindTrainersNotAssignedToTrainee() {
        Trainer first = createTrainer(
                "Anna",
                "Brown",
                "Anna.Brown",
                TrainingTypeName.YOGA,
                true
        );

        Trainer second = createTrainer(
                "Mike",
                "Jones",
                "Mike.Jones",
                TrainingTypeName.CARDIO,
                true
        );

        when(trainerRepository.findNotAssignedToTrainee(
                "John.Smith"
        )).thenReturn(
                List.of(first, second)
        );

        List<Trainer> result =
                trainerService.findNotAssignedToTrainee(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        assertEquals(2, result.size());
        assertTrue(result.contains(first));
        assertTrue(result.contains(second));

        verify(authenticationService)
                .requireTraineeAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        verify(trainerRepository)
                .findNotAssignedToTrainee(
                        "John.Smith"
                );
    }

    @Test
    void shouldThrowExceptionWhenTrainingTypeDoesNotExistDuringUpdate() {
        Trainer trainer = createTrainer();

        when(trainerRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainer));

        when(trainingTypeRepository.findByTrainingTypeName(
                TrainingTypeName.CARDIO
        )).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> trainerService.update(
                        "Jonathan",
                        "Johnson",
                        TrainingTypeName.CARDIO,
                        "John.Smith",
                        "Abc123xyZ9"
                )
        );

        verify(authenticationService)
                .requireTrainerAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        verify(trainingTypeRepository)
                .findByTrainingTypeName(
                        TrainingTypeName.CARDIO
                );

        assertEquals(
                "John",
                trainer.getUser().getFirstName()
        );

        assertEquals(
                TrainingTypeName.STRENGTH,
                trainer.getSpecialization()
                        .getTrainingTypeName()
        );
    }

    @Test
    void shouldThrowExceptionWhenTrainerDoesNotExistByUsername() {
        when(trainerRepository.findByUser_Username(
                "Unknown.Trainer"
        )).thenReturn(Optional.empty());

        NoSuchElementException exception =
                assertThrows(
                        NoSuchElementException.class,
                        () -> trainerService.findByUsername(
                                "Unknown.Trainer",
                                "password"
                        )
                );

        assertEquals(
                "Trainer with username Unknown.Trainer not found",
                exception.getMessage()
        );

        verify(authenticationService)
                .requireTrainerAuthentication(
                        "Unknown.Trainer",
                        "password"
                );

        verify(trainerRepository)
                .findByUser_Username(
                        "Unknown.Trainer"
                );
    }

    @Test
    void shouldReturnTrainerProfile() {
        Trainer trainer = createTrainer();

        Trainee traineeB = createTraineeMock(
                "Mike",
                "Jones",
                "Mike.Jones"
        );

        Trainee traineeA = createTraineeMock(
                "Anna",
                "Brown",
                "Anna.Brown"
        );

        trainer.getTrainees().add(traineeB);
        trainer.getTrainees().add(traineeA);

        when(trainerRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainer));

        TrainerProfileResponse result =
                trainerService.getProfile(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        assertEquals(
                "John",
                result.firstName()
        );

        assertEquals(
                "Smith",
                result.lastName()
        );

        assertEquals(
                TrainingTypeName.STRENGTH,
                result.specialization()
        );

        assertTrue(result.isActive());

        assertEquals(
                2,
                result.trainees().size()
        );

        assertEquals(
                "Anna.Brown",
                result.trainees()
                        .get(0)
                        .username()
        );

        assertEquals(
                "Mike.Jones",
                result.trainees()
                        .get(1)
                        .username()
        );

        assertEquals(
                "Anna",
                result.trainees()
                        .get(0)
                        .firstName()
        );

        assertEquals(
                "Brown",
                result.trainees()
                        .get(0)
                        .lastName()
        );

        verify(authenticationService)
                .requireTrainerAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );
    }

    @Test
    void shouldUpdateTrainerProfile() {
        Trainer trainer = createTrainer();

        when(trainerRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainer));

        TrainerUpdateResponse result =
                trainerService.updateProfile(
                        "John.Smith",
                        "Abc123xyZ9",
                        "Jonathan",
                        "Johnson",
                        false
                );

        assertEquals(
                "John.Smith",
                result.username()
        );

        assertEquals(
                "Jonathan",
                result.firstName()
        );

        assertEquals(
                "Johnson",
                result.lastName()
        );

        assertEquals(
                TrainingTypeName.STRENGTH,
                result.specialization()
        );

        assertFalse(
                result.isActive()
        );

        assertTrue(
                result.trainees().isEmpty()
        );

        assertEquals(
                "Jonathan",
                trainer.getUser().getFirstName()
        );

        assertEquals(
                "Johnson",
                trainer.getUser().getLastName()
        );

        assertFalse(
                trainer.getUser().isActive()
        );

        verify(authenticationService)
                .requireTrainerAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        verify(trainerRepository)
                .findByUser_Username(
                        "John.Smith"
                );
    }
    @Test
    void shouldReturnUnassignedTrainerSummaries() {
        Trainer first = createTrainer(
                "Anna",
                "Brown",
                "Anna.Brown",
                TrainingTypeName.YOGA,
                true
        );

        Trainer second = createTrainer(
                "Mike",
                "Jones",
                "Mike.Jones",
                TrainingTypeName.CARDIO,
                true
        );

        when(trainerRepository.findNotAssignedToTrainee(
                "John.Smith"
        )).thenReturn(
                List.of(first, second)
        );

        List<TrainerSummary> result =
                trainerService.getUnassignedTrainers(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                "Anna.Brown",
                result.getFirst().username()
        );

        assertEquals(
                "Anna",
                result.getFirst().firstName()
        );

        assertEquals(
                "Brown",
                result.get(0).lastName()
        );

        assertEquals(
                TrainingTypeName.YOGA,
                result.get(0).specialization()
        );

        assertEquals(
                "Mike.Jones",
                result.get(1).username()
        );

        assertEquals(
                TrainingTypeName.CARDIO,
                result.get(1).specialization()
        );

        verify(authenticationService)
                .requireTraineeAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        verify(trainerRepository)
                .findNotAssignedToTrainee(
                        "John.Smith"
                );
    }

    private Trainee createTraineeMock(
            String firstName,
            String lastName,
            String username
    ) {
        Trainee trainee = mock(Trainee.class);

        User user = new User(
                firstName,
                lastName,
                username,
                "Trainee123",
                true
        );

        when(trainee.getUser())
                .thenReturn(user);

        return trainee;
    }

    private Trainer createTrainer() {
        return createTrainer(
                "John",
                "Smith",
                "John.Smith",
                TrainingTypeName.STRENGTH,
                true
        );
    }

    private Trainer createTrainer(
            String firstName,
            String lastName,
            String username,
            TrainingTypeName specialization,
            boolean active
    ) {
        User user = new User(
                firstName,
                lastName,
                username,
                "Abc123xyZ9",
                active
        );

        TrainingType trainingType =
                new TrainingType(
                        specialization
                );

        return new Trainer(
                trainingType,
                user
        );
    }
}