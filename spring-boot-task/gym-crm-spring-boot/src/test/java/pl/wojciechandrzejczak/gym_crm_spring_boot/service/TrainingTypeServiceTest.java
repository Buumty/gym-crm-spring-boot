package pl.wojciechandrzejczak.gym_crm_spring_boot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TrainingTypeResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingType;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainingTypeRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication.AuthenticationService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private TrainingTypeService trainingTypeService;

    @Test
    void shouldReturnAllTrainingTypes() {
        TrainingType strength =
                new TrainingType(
                        TrainingTypeName.STRENGTH
                );

        TrainingType cardio =
                new TrainingType(
                        TrainingTypeName.CARDIO
                );

        when(trainingTypeRepository.findAll())
                .thenReturn(
                        List.of(
                                strength,
                                cardio
                        )
                );

        List<TrainingTypeResponse> result =
                trainingTypeService.findAll(
                        "John.Smith",
                        "password"
                );

        assertEquals(
                2,
                result.size()
        );

        assertEquals(
                TrainingTypeName.STRENGTH,
                result.get(0)
                        .trainingType()
        );

        assertEquals(
                TrainingTypeName.CARDIO,
                result.get(1)
                        .trainingType()
        );

        verify(authenticationService)
                .requireAuthentication(
                        "John.Smith",
                        "password"
                );

        verify(trainingTypeRepository)
                .findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoTrainingTypesExist() {
        when(trainingTypeRepository.findAll())
                .thenReturn(List.of());

        List<TrainingTypeResponse> result =
                trainingTypeService.findAll(
                        "John.Smith",
                        "password"
                );

        assertTrue(result.isEmpty());

        verify(authenticationService)
                .requireAuthentication(
                        "John.Smith",
                        "password"
                );

        verify(trainingTypeRepository)
                .findAll();
    }
}