package pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainee;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainer;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TraineeRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainerRepository;

@Service
@Transactional(readOnly = true)
public class AuthenticationService {
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public AuthenticationService(TraineeRepository traineeRepository, TrainerRepository trainerRepository) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
    }

    public boolean traineeCredentialsValidation(String username, String password) {
        return traineeRepository.findByUser_Username(username)
                .map(Trainee::getUser)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);

    }

    public boolean trainerCredentialsValidation(String username, String password) {
        return trainerRepository.findByUser_Username(username)
                .map(Trainer::getUser)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);

    }

    public void requireTrainerAuthentication(
            String username,
            String password
    ) {
        if (!trainerCredentialsValidation(username, password)) {
            throw new SecurityException("Invalid credentials");
        }
    }
    public void requireTraineeAuthentication(
            String username,
            String password
    ) {
        if (!traineeCredentialsValidation(username, password)) {
            throw new SecurityException("Invalid credentials");
        }
    }
    public void requireAuthentication(
            String username,
            String password
    ) {
        boolean traineeAuthenticated =
                traineeCredentialsValidation(username, password);

        boolean trainerAuthenticated =
                trainerCredentialsValidation(username, password);

        if (!traineeAuthenticated && !trainerAuthenticated) {
            throw new SecurityException("Invalid credentials");
        }
    }
}
