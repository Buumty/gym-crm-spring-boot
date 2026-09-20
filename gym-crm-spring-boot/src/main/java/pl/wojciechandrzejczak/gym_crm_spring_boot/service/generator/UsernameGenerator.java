package org.example.service.generator;

import org.example.dao.TraineeDao;
import org.example.dao.TrainerDao;
import org.springframework.stereotype.Component;

@Component
public class UsernameGenerator {
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;

    public UsernameGenerator(TraineeDao traineeDao, TrainerDao trainerDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
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
        return traineeDao.existsByUsername(username)
                || trainerDao.existsByUsername(username);
    }
}
