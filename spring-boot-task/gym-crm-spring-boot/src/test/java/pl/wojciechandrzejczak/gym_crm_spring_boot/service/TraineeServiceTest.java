package pl.wojciechandrzejczak.gym_crm_spring_boot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainee.TraineeProfileResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainee.TraineeUpdateResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerSummary;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.*;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TraineeRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainerRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication.AuthenticationService;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.generator.PasswordGenerator;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.generator.UsernameGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private PasswordGenerator passwordGenerator;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private TraineeService traineeService;

    @Test
    void shouldFindTraineeById() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findById(1L))
                .thenReturn(Optional.of(trainee));

        Trainee result = traineeService.findById(
                1L,
                "John.Smith",
                "Abc123xyZ9"
        );

        assertSame(trainee, result);

        verify(authenticationService)
                .requireTraineeAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        verify(traineeRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTraineeDoesNotExistById() {
        when(traineeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> traineeService.findById(
                        999L,
                        "John.Smith",
                        "Abc123xyZ9"
                )
        );

        verify(authenticationService)
                .requireTraineeAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        verify(traineeRepository).findById(999L);
    }

    @Test
    void shouldReturnAllTrainees() {
        Trainee first = createTrainee();

        Trainee second = createTrainee(
                "Anna",
                "Brown",
                "Anna.Brown",
                true
        );

        when(traineeRepository.findAll())
                .thenReturn(List.of(first, second));

        List<Trainee> result =
                traineeService.findAll(
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

        verify(traineeRepository).findAll();
    }

    @Test
    void shouldCreateTrainee() {
        LocalDate dateOfBirth =
                LocalDate.of(1995, 5, 10);

        when(usernameGenerator.generate(
                "John",
                "Smith"
        )).thenReturn("John.Smith");

        when(passwordGenerator.generate())
                .thenReturn("Abc123xyZ9");

        when(traineeRepository.save(any(Trainee.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        Trainee result = traineeService.create(
                "John",
                "Smith",
                dateOfBirth,
                "Example address"
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

        assertEquals(
                dateOfBirth,
                result.getDateOfBirth()
        );

        assertEquals(
                "Example address",
                result.getAddress()
        );

        verify(usernameGenerator)
                .generate("John", "Smith");

        verify(passwordGenerator)
                .generate();

        verify(traineeRepository)
                .save(any(Trainee.class));
    }

    @Test
    void shouldUpdateTrainee() {
        Trainee trainee = createTrainee();

        LocalDate newDateOfBirth =
                LocalDate.of(1996, 6, 15);

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        Trainee result = traineeService.update(
                "Jonathan",
                "Johnson",
                "New address",
                newDateOfBirth,
                "John.Smith",
                "Abc123xyZ9"
        );

        assertSame(trainee, result);

        assertEquals(
                "Jonathan",
                result.getUser().getFirstName()
        );

        assertEquals(
                "Johnson",
                result.getUser().getLastName()
        );

        assertEquals(
                "New address",
                result.getAddress()
        );

        assertEquals(
                "John.Smith",
                result.getUser().getUsername()
        );

        assertEquals(
                "Abc123xyZ9",
                result.getUser().getPassword()
        );
        assertEquals(
                newDateOfBirth,
                result.getDateOfBirth()
        );

        verify(authenticationService)
                .requireTraineeAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        verify(traineeRepository)
                .findByUser_Username("John.Smith");

    }

    @Test
    void shouldDeleteTraineeByUsername() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        traineeService.deleteByUsername(
                "John.Smith",
                "Abc123xyZ9"
        );

        verify(authenticationService)
                .requireTraineeAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        verify(traineeRepository)
                .findByUser_Username("John.Smith");

        verify(traineeRepository)
                .delete(trainee);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingTrainee() {
        when(traineeRepository.findByUser_Username(
                "Unknown.User"
        )).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> traineeService.deleteByUsername(
                        "Unknown.User",
                        "password"
                )
        );

        verify(authenticationService)
                .requireTraineeAuthentication(
                        "Unknown.User",
                        "password"
                );

        verify(traineeRepository, never())
                .delete(any());
    }

    @Test
    void shouldFindTraineeByUsername() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        Trainee result =
                traineeService.findByUsername(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        assertSame(trainee, result);

        verify(authenticationService)
                .requireTraineeAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );
    }

    @Test
    void shouldChangePassword() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        traineeService.changePassword(
                "John.Smith",
                "Abc123xyZ9",
                "NewPassword1"
        );

        assertEquals(
                "NewPassword1",
                trainee.getUser().getPassword()
        );

        verify(authenticationService)
                .requireTraineeAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );


    }

    @Test
    void shouldActivateInactiveTrainee() {
        Trainee trainee = createTrainee(
                "John",
                "Smith",
                "John.Smith",
                false
        );

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        traineeService.activate(
                "John.Smith",
                "Abc123xyZ9"
        );

        assertTrue(
                trainee.getUser().isActive()
        );
    }

    @Test
    void shouldThrowExceptionWhenActivatingAlreadyActiveTrainee() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        assertThrows(
                IllegalStateException.class,
                () -> traineeService.activate(
                        "John.Smith",
                        "Abc123xyZ9"
                )
        );
    }

    @Test
    void shouldDeactivateActiveTrainee() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        traineeService.deactivate(
                "John.Smith",
                "Abc123xyZ9"
        );

        assertFalse(
                trainee.getUser().isActive()
        );
    }

    @Test
    void shouldThrowExceptionWhenDeactivatingAlreadyInactiveTrainee() {
        Trainee trainee = createTrainee(
                "John",
                "Smith",
                "John.Smith",
                false
        );

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        assertThrows(
                IllegalStateException.class,
                () -> traineeService.deactivate(
                        "John.Smith",
                        "Abc123xyZ9"
                )
        );
    }

    @Test
    void shouldUpdateTrainersList() {
        Trainee trainee = createTrainee();

        Trainer trainer1 = createTrainer(
                "Anna",
                "Brown",
                "Anna.Brown"
        );

        Trainer trainer2 = createTrainer(
                "Mike",
                "Jones",
                "Mike.Jones"
        );

        Set<String> usernames = Set.of(
                "Anna.Brown",
                "Mike.Jones"
        );

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUser_UsernameIn(
                usernames
        )).thenReturn(
                List.of(trainer1, trainer2)
        );

        Trainee result =
                traineeService.updateTrainers(
                        "John.Smith",
                        "Abc123xyZ9",
                        usernames
                );

        assertEquals(
                2,
                result.getTrainers().size()
        );

        assertTrue(
                result.getTrainers()
                        .contains(trainer1)
        );

        assertTrue(
                result.getTrainers()
                        .contains(trainer2)
        );

        verify(trainerRepository)
                .findByUser_UsernameIn(usernames);
    }

    @Test
    void shouldRemoveAllTrainersWhenUsernameSetIsEmpty() {
        Trainee trainee = createTrainee();

        Trainer trainer = createTrainer(
                "Anna",
                "Brown",
                "Anna.Brown"
        );

        trainee.getTrainers().add(trainer);

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        Trainee result =
                traineeService.updateTrainers(
                        "John.Smith",
                        "Abc123xyZ9",
                        Set.of()
                );

        assertTrue(
                result.getTrainers().isEmpty()
        );

        verify(trainerRepository, never())
                .findByUser_UsernameIn(any());
    }

    @Test
    void shouldThrowExceptionWhenTrainerUsernamesAreNull() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        assertThrows(
                IllegalArgumentException.class,
                () -> traineeService.updateTrainers(
                        "John.Smith",
                        "Abc123xyZ9",
                        null
                )
        );

        verify(trainerRepository, never())
                .findByUser_UsernameIn(any());
    }

    @Test
    void shouldThrowExceptionWhenOneOrMoreTrainersDoNotExist() {
        Trainee trainee = createTrainee();

        Trainer trainer = createTrainer(
                "Anna",
                "Brown",
                "Anna.Brown"
        );

        Set<String> usernames = Set.of(
                "Anna.Brown",
                "Unknown.Trainer"
        );

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUser_UsernameIn(
                usernames
        )).thenReturn(
                List.of(trainer)
        );

        assertThrows(
                NoSuchElementException.class,
                () -> traineeService.updateTrainers(
                        "John.Smith",
                        "Abc123xyZ9",
                        usernames
                )
        );

        assertTrue(
                trainee.getTrainers().isEmpty()
        );
    }

    private Trainee createTrainee() {
        return createTrainee(
                "John",
                "Smith",
                "John.Smith",
                true
        );
    }

    private Trainee createTrainee(
            String firstName,
            String lastName,
            String username,
            boolean active
    ) {
        User user = new User(
                firstName,
                lastName,
                username,
                "Abc123xyZ9",
                active
        );

        return new Trainee(
                user,
                LocalDate.of(1995, 5, 10),
                "Example address"
        );
    }

    private Trainer createTrainer(
            String firstName,
            String lastName,
            String username
    ) {
        User user = new User(
                firstName,
                lastName,
                username,
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

    @Test
    void shouldReturnTraineeProfile() {
        Trainee trainee = createTrainee();

        Trainer trainerB = createTrainer(
                "Mike",
                "Jones",
                "Mike.Jones"
        );

        Trainer trainerA = createTrainer(
                "Anna",
                "Brown",
                "Anna.Brown"
        );

        trainee.getTrainers().add(trainerB);
        trainee.getTrainers().add(trainerA);

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        TraineeProfileResponse result =
                traineeService.getProfile(
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
                LocalDate.of(1995, 5, 10),
                result.dateOfBirth()
        );

        assertEquals(
                "Example address",
                result.address()
        );

        assertTrue(result.isActive());

        assertEquals(
                2,
                result.trainers().size()
        );

        assertEquals(
                "Anna.Brown",
                result.trainers()
                        .get(0)
                        .username()
        );

        assertEquals(
                "Mike.Jones",
                result.trainers()
                        .get(1)
                        .username()
        );

        verify(authenticationService)
                .requireTraineeAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        verify(traineeRepository)
                .findByUser_Username("John.Smith");
    }

    @Test
    void shouldUpdateTraineeProfile() {
        Trainee trainee = createTrainee();

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        LocalDate newDateOfBirth =
                LocalDate.of(1996, 6, 15);

        TraineeUpdateResponse result =
                traineeService.updateProfile(
                        "John.Smith",
                        "Abc123xyZ9",
                        "Jonathan",
                        "Johnson",
                        newDateOfBirth,
                        "New address",
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
                newDateOfBirth,
                result.dateOfBirth()
        );

        assertEquals(
                "New address",
                result.address()
        );

        assertFalse(result.isActive());

        assertTrue(
                result.trainers().isEmpty()
        );

        assertEquals(
                "Jonathan",
                trainee.getUser().getFirstName()
        );

        assertEquals(
                "Johnson",
                trainee.getUser().getLastName()
        );

        assertFalse(
                trainee.getUser().isActive()
        );

        verify(authenticationService)
                .requireTraineeAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        verify(traineeRepository)
                .findByUser_Username("John.Smith");
    }

    @Test
    void shouldUpdateTrainersListAndReturnTrainerSummaries() {
        Trainee trainee = createTrainee();

        Trainer trainer1 = createTrainer(
                "Anna",
                "Brown",
                "Anna.Brown"
        );

        Trainer trainer2 = createTrainer(
                "Mike",
                "Jones",
                "Mike.Jones"
        );

        Set<String> usernames = Set.of(
                "Anna.Brown",
                "Mike.Jones"
        );

        when(traineeRepository.findByUser_Username(
                "John.Smith"
        )).thenReturn(Optional.of(trainee));

        when(trainerRepository.findByUser_UsernameIn(
                usernames
        )).thenReturn(
                List.of(
                        trainer1,
                        trainer2
                )
        );

        List<TrainerSummary> result =
                traineeService.updateTrainersList(
                        "John.Smith",
                        "Abc123xyZ9",
                        usernames
                );

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                "Anna.Brown",
                result.get(0).username()
        );

        assertEquals(
                "Mike.Jones",
                result.get(1).username()
        );

        assertEquals(
                "Anna",
                result.get(0).firstName()
        );

        assertEquals(
                "Brown",
                result.get(0).lastName()
        );

        assertEquals(
                TrainingTypeName.STRENGTH,
                result.get(0).specialization()
        );

        verify(authenticationService)
                .requireTraineeAuthentication(
                        "John.Smith",
                        "Abc123xyZ9"
                );

        verify(trainerRepository)
                .findByUser_UsernameIn(usernames);
    }
}