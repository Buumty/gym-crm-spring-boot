package pl.wojciechandrzejczak.gym_crm_spring_boot.service.generator;

import org.springframework.stereotype.Component;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TraineeRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainerRepository;

@Component
public class UsernameGenerator {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public UsernameGenerator(TraineeRepository traineeRepository, TrainerRepository trainerRepository) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
    }

    public String generate(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int suffix = 1;

        while (exists(username)) {
            username = baseUsername + suffix;
            suffix++;
        }

        return username;
    }

    private boolean exists(String username) {
        return traineeRepository.existsByUser_Username(username)
                || trainerRepository.existsByUser_Username(username);
    }
}
