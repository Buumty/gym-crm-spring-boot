package pl.wojciechandrzejczak.gym_crm_spring_boot.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {
    private final Counter traineeRegistrations;
    private final Counter trainerRegistrations;
    private final Counter trainingsCreated;

    public GymMetrics(MeterRegistry meterRegistry) {
        this.traineeRegistrations = Counter.builder("gymcrm.trainee.registrations")
                .description("Number of successfully registered trainees")
                .register(meterRegistry);

        this.trainerRegistrations = Counter.builder("gymcrm.trainer.registrations")
                .description("Number of successfully registered trainers")
                .register(meterRegistry);

        this.trainingsCreated = Counter.builder("gymcrm.trainings.created")
                .description("Number of successfully created trainings")
                .register(meterRegistry);
    }

    public void incrementTraineeRegistrations() {
        traineeRegistrations.increment();
    }
    public void incrementTrainerRegistrations() {
        trainerRegistrations.increment();
    }
    public void incrementTrainingsCreated() {
        trainingsCreated.increment();
    }
}
