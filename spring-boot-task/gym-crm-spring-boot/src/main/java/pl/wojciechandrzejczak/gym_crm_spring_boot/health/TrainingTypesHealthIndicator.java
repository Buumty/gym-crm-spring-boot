package pl.wojciechandrzejczak.gym_crm_spring_boot.health;

import org.jspecify.annotations.Nullable;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainingTypeRepository;

@Component
public class TrainingTypesHealthIndicator implements HealthIndicator {
    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypesHealthIndicator(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    public Health health() {
        long count = trainingTypeRepository.count();

        if (count > 0) {
            return Health.up()
                    .withDetail("trainingTypesCount", count)
                    .build();
        }

        return Health.down()
                .withDetail("reason", "No training types available")
                .build();
    }
}
