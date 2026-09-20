package pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.*;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TraineeRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainerRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldValidateTraineeCredentials() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUser_Username("John.Smith"))
                .thenReturn(Optional.of(trainee));

        boolean result =
                authenticationService.traineeCredentialsValidation(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        assertTrue(result);

        verify(traineeRepository)
                .findByUser_Username("John.Smith");
    }

    @Test
    void shouldReturnFalseWhenTraineePasswordIsInvalid() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUser_Username("John.Smith"))
                .thenReturn(Optional.of(trainee));

        boolean result =
                authenticationService.traineeCredentialsValidation(
                        "John.Smith",
                        "WrongPassword"
                );

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenTraineeDoesNotExist() {
        when(traineeRepository.findByUser_Username("Unknown.User"))
                .thenReturn(Optional.empty());

        boolean result =
                authenticationService.traineeCredentialsValidation(
                        "Unknown.User",
                        "password"
                );

        assertFalse(result);
    }

    @Test
    void shouldValidateTrainerCredentials() {
        Trainer trainer = createTrainer();

        when(trainerRepository.findByUser_Username("Anna.Brown"))
                .thenReturn(Optional.of(trainer));

        boolean result =
                authenticationService.trainerCredentialsValidation(
                        "Anna.Brown",
                        "Trainer123"
                );

        assertTrue(result);

        verify(trainerRepository)
                .findByUser_Username("Anna.Brown");
    }

    @Test
    void shouldReturnFalseWhenTrainerPasswordIsInvalid() {
        Trainer trainer = createTrainer();

        when(trainerRepository.findByUser_Username("Anna.Brown"))
                .thenReturn(Optional.of(trainer));

        boolean result =
                authenticationService.trainerCredentialsValidation(
                        "Anna.Brown",
                        "WrongPassword"
                );

        assertFalse(result);
    }

    @Test
    void shouldReturnFalseWhenTrainerDoesNotExist() {
        when(trainerRepository.findByUser_Username("Unknown.Trainer"))
                .thenReturn(Optional.empty());

        boolean result =
                authenticationService.trainerCredentialsValidation(
                        "Unknown.Trainer",
                        "password"
                );

        assertFalse(result);
    }

    @Test
    void shouldAuthenticateTrainee() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUser_Username("John.Smith"))
                .thenReturn(Optional.of(trainee));

        assertDoesNotThrow(
                () -> authenticationService
                        .requireTraineeAuthentication(
                                "John.Smith",
                                "Abc123xyZ9"
                        )
        );
    }

    @Test
    void shouldThrowExceptionWhenTraineeAuthenticationFails() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUser_Username("John.Smith"))
                .thenReturn(Optional.of(trainee));

        assertThrows(
                SecurityException.class,
                () -> authenticationService
                        .requireTraineeAuthentication(
                                "John.Smith",
                                "WrongPassword"
                        )
        );
    }

    @Test
    void shouldAuthenticateTrainer() {
        Trainer trainer = createTrainer();

        when(trainerRepository.findByUser_Username("Anna.Brown"))
                .thenReturn(Optional.of(trainer));

        assertDoesNotThrow(
                () -> authenticationService
                        .requireTrainerAuthentication(
                                "Anna.Brown",
                                "Trainer123"
                        )
        );
    }

    @Test
    void shouldThrowExceptionWhenTrainerAuthenticationFails() {
        Trainer trainer = createTrainer();

        when(trainerRepository.findByUser_Username("Anna.Brown"))
                .thenReturn(Optional.of(trainer));

        assertThrows(
                SecurityException.class,
                () -> authenticationService
                        .requireTrainerAuthentication(
                                "Anna.Brown",
                                "WrongPassword"
                        )
        );
    }

    @Test
    void shouldAuthenticateWhenCredentialsBelongToTrainee() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUser_Username("John.Smith"))
                .thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUser_Username("John.Smith"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(
                () -> authenticationService.requireAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                )
        );
    }

    @Test
    void shouldAuthenticateWhenCredentialsBelongToTrainer() {
        Trainer trainer = createTrainer();

        when(traineeRepository.findByUser_Username("Anna.Brown"))
                .thenReturn(Optional.empty());

        when(trainerRepository.findByUser_Username("Anna.Brown"))
                .thenReturn(Optional.of(trainer));

        assertDoesNotThrow(
                () -> authenticationService.requireAuthentication(
                        "Anna.Brown",
                        "Trainer123"
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenGenericAuthenticationFails() {
        when(traineeRepository.findByUser_Username("Unknown.User"))
                .thenReturn(Optional.empty());

        when(trainerRepository.findByUser_Username("Unknown.User"))
                .thenReturn(Optional.empty());

        assertThrows(
                SecurityException.class,
                () -> authenticationService.requireAuthentication(
                        "Unknown.User",
                        "password"
                )
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

    private Trainer createTrainer() {
        User user = new User(
                "Anna",
                "Brown",
                "Anna.Brown",
                "Trainer123",
                true
        );

        TrainingType trainingType =
                new TrainingType(
                        TrainingTypeName.STRENGTH
                );

        return new Trainer(
                trainingType,
                user
        );
    }
}