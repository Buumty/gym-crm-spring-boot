package pl.wojciechandrzejczak.gym_crm_spring_boot.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingType;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainingTypeRepository;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class TrainingTypeInitHealthIndicator implements HealthIndicator {

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeInitHealthIndicator(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public Health health() {
        Set<TrainingTypeName> expectedTypes =
                Set.copyOf(Arrays.asList(TrainingTypeName.values()));

        Set<TrainingTypeName> actualTypes =
                trainingTypeRepository.findAll()
                        .stream()
                        .map(TrainingType::getTrainingTypeName)
                        .collect(Collectors.toSet());

        if (actualTypes.equals(expectedTypes)) {
            return Health.up()
                    .withDetail("trainingTypes", "Successful initialization")
                    .build();
        }

        return Health.down()
                .withDetail("trainingTypes", "Failed initialization")
                .withDetail("expected", expectedTypes)
                .withDetail("actual", actualTypes)
                .build();
    }
}
