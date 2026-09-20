package pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.TraineeService;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.TrainerService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @InjectMocks
    private PasswordService passwordService;

    @Test
    void shouldChangeTraineePasswordWhenTraineeCredentialsAreValid() {
        String username = "John.Smith";
        String oldPassword = "OldPassword1";
        String newPassword = "NewPassword1";

        when(authenticationService.traineeCredentialsValidation(
                username,
                oldPassword
        )).thenReturn(true);

        passwordService.changePassword(
                username,
                oldPassword,
                newPassword
        );

        verify(authenticationService)
                .traineeCredentialsValidation(
                        username,
                        oldPassword
                );

        verify(traineeService)
                .changePassword(
                        username,
                        oldPassword,
                        newPassword
                );

        verify(authenticationService, never())
                .trainerCredentialsValidation(
                        anyString(),
                        anyString()
                );

        verifyNoInteractions(trainerService);
    }

    @Test
    void shouldChangeTrainerPasswordWhenTrainerCredentialsAreValid() {
        String username = "Anna.Brown";
        String oldPassword = "OldPassword1";
        String newPassword = "NewPassword1";

        when(authenticationService.traineeCredentialsValidation(
                username,
                oldPassword
        )).thenReturn(false);

        when(authenticationService.trainerCredentialsValidation(
                username,
                oldPassword
        )).thenReturn(true);

        passwordService.changePassword(
                username,
                oldPassword,
                newPassword
        );

        verify(authenticationService)
                .traineeCredentialsValidation(
                        username,
                        oldPassword
                );

        verify(authenticationService)
                .trainerCredentialsValidation(
                        username,
                        oldPassword
                );

        verify(trainerService)
                .changePassword(
                        username,
                        oldPassword,
                        newPassword
                );

        verifyNoInteractions(traineeService);
    }

    @Test
    void shouldThrowSecurityExceptionWhenCredentialsAreInvalid() {
        String username = "Unknown.User";
        String oldPassword = "WrongPassword";
        String newPassword = "NewPassword1";

        when(authenticationService.traineeCredentialsValidation(
                username,
                oldPassword
        )).thenReturn(false);

        when(authenticationService.trainerCredentialsValidation(
                username,
                oldPassword
        )).thenReturn(false);

        SecurityException exception =
                assertThrows(
                        SecurityException.class,
                        () -> passwordService.changePassword(
                                username,
                                oldPassword,
                                newPassword
                        )
                );

        assertEquals(
                "Invalid credentials",
                exception.getMessage()
        );

        verify(authenticationService)
                .traineeCredentialsValidation(
                        username,
                        oldPassword
                );

        verify(authenticationService)
                .trainerCredentialsValidation(
                        username,
                        oldPassword
                );

        verifyNoInteractions(
                traineeService,
                trainerService
        );
    }
}