package pl.wojciechandrzejczak.gym_crm_spring_boot.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainingTypeRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TrainingTypesHealthIndicatorTest {

    private TrainingTypeRepository trainingTypeRepository;
    private TrainingTypesHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        trainingTypeRepository = mock(TrainingTypeRepository.class);
        healthIndicator = new TrainingTypesHealthIndicator(trainingTypeRepository);
    }

    @Test
    void shouldReturnUpWhenTrainingTypesExist() {
        when(trainingTypeRepository.count()).thenReturn(4L);

        Health health = healthIndicator.health();

        assert health != null;
        assertEquals(Status.UP, health.getStatus());
        assertEquals(4L, health.getDetails().get("trainingTypesCount"));
    }

    @Test
    void shouldReturnDownWhenNoTrainingTypesExist() {
        when(trainingTypeRepository.count()).thenReturn(0L);

        Health health = healthIndicator.health();

        assert health != null;
        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(
                "No training types available",
                health.getDetails().get("reason")
        );
    }
}