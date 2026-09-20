package pl.wojciechandrzejczak.gym_crm_spring_boot.initializer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingType;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainingTypeRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingTypeInitializerTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeInitializer initializer;

    @Test
    void shouldInitializeAllTrainingTypesWhenDatabaseIsEmpty() {
        for (TrainingTypeName name : TrainingTypeName.values()) {
            when(trainingTypeRepository.findByTrainingTypeName(name))
                    .thenReturn(Optional.empty());
        }

        initializer.initializeTrainingTypes();

        ArgumentCaptor<TrainingType> captor =
                ArgumentCaptor.forClass(TrainingType.class);

        verify(
                trainingTypeRepository,
                times(TrainingTypeName.values().length)
        ).save(captor.capture());

        List<TrainingTypeName> savedTypes =
                captor.getAllValues()
                        .stream()
                        .map(TrainingType::getTrainingTypeName)
                        .toList();

        assertEquals(
                TrainingTypeName.values().length,
                savedTypes.size()
        );

        for (TrainingTypeName name : TrainingTypeName.values()) {
            assertTrue(savedTypes.contains(name));

            verify(trainingTypeRepository)
                    .findByTrainingTypeName(name);
        }
    }

    @Test
    void shouldNotSaveTrainingTypesWhenTheyAlreadyExist() {
        for (TrainingTypeName name : TrainingTypeName.values()) {
            when(trainingTypeRepository.findByTrainingTypeName(name))
                    .thenReturn(
                            Optional.of(
                                    new TrainingType(name)
                            )
                    );
        }

        initializer.initializeTrainingTypes();

        verify(trainingTypeRepository, never())
                .save(any(TrainingType.class));
    }

    @Test
    void shouldSaveOnlyMissingTrainingTypes() {
        TrainingType fitness =
                new TrainingType(
                        TrainingTypeName.FITNESS
                );

        TrainingType strength =
                new TrainingType(
                        TrainingTypeName.STRENGTH
                );

        when(trainingTypeRepository.findByTrainingTypeName(
                TrainingTypeName.FITNESS
        )).thenReturn(Optional.of(fitness));

        when(trainingTypeRepository.findByTrainingTypeName(
                TrainingTypeName.STRENGTH
        )).thenReturn(Optional.of(strength));

        when(trainingTypeRepository.findByTrainingTypeName(
                TrainingTypeName.CARDIO
        )).thenReturn(Optional.empty());

        when(trainingTypeRepository.findByTrainingTypeName(
                TrainingTypeName.YOGA
        )).thenReturn(Optional.empty());

        initializer.initializeTrainingTypes();

        ArgumentCaptor<TrainingType> captor =
                ArgumentCaptor.forClass(
                        TrainingType.class
                );

        verify(trainingTypeRepository, times(2))
                .save(captor.capture());

        List<TrainingTypeName> savedTypes =
                captor.getAllValues()
                        .stream()
                        .map(TrainingType::getTrainingTypeName)
                        .toList();

        assertEquals(2, savedTypes.size());
        assertTrue(
                savedTypes.contains(
                        TrainingTypeName.CARDIO
                )
        );
        assertTrue(
                savedTypes.contains(
                        TrainingTypeName.YOGA
                )
        );

        assertFalse(
                savedTypes.contains(
                        TrainingTypeName.FITNESS
                )
        );
        assertFalse(
                savedTypes.contains(
                        TrainingTypeName.STRENGTH
                )
        );
    }
}