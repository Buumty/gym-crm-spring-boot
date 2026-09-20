package org.example.service.authentication;

import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthenticationService {
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;

    public AuthenticationService(TraineeDao traineeDao, TrainerDao trainerDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
    }

    public boolean traineeCredentialsValidation(String username, String password) {
        return traineeDao.findByUsername(username)
                .map(Trainee::getUser)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);

    }

    public boolean trainerCredentialsValidation(String username, String password) {
        return trainerDao.findByUsername(username)
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
