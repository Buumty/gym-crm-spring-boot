package pl.wojciechandrzejczak.gym_crm_spring_boot.initializer;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingType;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainingTypeRepository;

@Component
public class TrainingTypeInitializer {
    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeInitializer(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void initializeTrainingTypes() {
        for (TrainingTypeName name : TrainingTypeName.values()) {

            if (trainingTypeRepository.findByTrainingTypeName(name).isEmpty()) {
                trainingTypeRepository.save(new TrainingType(name));
            }
        }
    }
}
