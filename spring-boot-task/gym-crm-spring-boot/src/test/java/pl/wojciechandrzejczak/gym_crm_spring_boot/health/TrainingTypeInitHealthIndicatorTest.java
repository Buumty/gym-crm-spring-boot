package pl.wojciechandrzejczak.gym_crm_spring_boot.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingType;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainingTypeRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrainingTypeInitHealthIndicatorTest {

    private TrainingTypeRepository trainingTypeRepository;
    private TrainingTypeInitHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        trainingTypeRepository = mock(TrainingTypeRepository.class);
        healthIndicator =
                new TrainingTypeInitHealthIndicator(trainingTypeRepository);
    }

    @Test
    void shouldReturnUpWhenAllTrainingTypesAreInitialized() {
        when(trainingTypeRepository.findAll()).thenReturn(List.of(
                new TrainingType(TrainingTypeName.FITNESS),
                new TrainingType(TrainingTypeName.STRENGTH),
                new TrainingType(TrainingTypeName.CARDIO),
                new TrainingType(TrainingTypeName.YOGA)
        ));

        Health health = healthIndicator.health();

        assert health != null;
        assertEquals(Status.UP, health.getStatus());
        assertEquals(
                "Successful initialization",
                health.getDetails().get("trainingTypes")
        );
    }

    @Test
    void shouldReturnDownWhenTrainingTypesAreMissing() {
        when(trainingTypeRepository.findAll()).thenReturn(List.of(
                new TrainingType(TrainingTypeName.FITNESS),
                new TrainingType(TrainingTypeName.STRENGTH),
                new TrainingType(TrainingTypeName.CARDIO)
        ));

        Health health = healthIndicator.health();

        assert health != null;
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(
                "Failed initialization",
                health.getDetails().get("trainingTypes")
        );
    }
}