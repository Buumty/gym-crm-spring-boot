package pl.wojciechandrzejczak.gym_crm_spring_boot.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainee.TraineeSummary;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerProfileResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerSummary;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerUpdateResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainer;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingType;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.User;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication.AuthenticationService;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.generator.PasswordGenerator;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.generator.UsernameGenerator;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Validated
@Transactional(readOnly = true)
public class TrainerService {
    private static final Logger log =
            LoggerFactory.getLogger(TrainerService.class);

    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;
    private final AuthenticationService authenticationService;

    public TrainerService(TrainerDao trainerDao, TrainingTypeDao trainingTypeDao, PasswordGenerator passwordGenerator, UsernameGenerator usernameGenerator, AuthenticationService authenticationService) {
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.passwordGenerator = passwordGenerator;
        this.usernameGenerator = usernameGenerator;
        this.authenticationService = authenticationService;
    }

    @Transactional
    public Trainer create(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotNull TrainingTypeName specialization) {
        User user = new User(firstName,
                lastName,
                usernameGenerator.generate(firstName, lastName),
                passwordGenerator.generate(),
                true);

        TrainingType trainingType = trainingTypeDao.findByName(specialization).orElseThrow(() ->
                new NoSuchElementException(
                        "Training type " + specialization + " not found"
                ));


        Trainer savedTrainer = trainerDao.save(new Trainer(trainingType,
                user));



        log.info(
                "Created trainer id={}, username={}",
                savedTrainer.getTrainerId(),
                savedTrainer.getUser().getUsername()
        );

        return savedTrainer;
    }

    @Transactional
    public Trainer update(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotNull TrainingTypeName specialization,
            @NotBlank String username,
            @NotBlank String password
    ) {
        authenticationService.requireTrainerAuthentication(username,password);
        Trainer trainer = getByUsername(username);

        TrainingType trainingType = trainingTypeDao.findByName(specialization).orElseThrow(() ->
                new NoSuchElementException(
                        "Training type " + specialization + " not found"
                ));

        trainer.getUser().setFirstName(firstName);
        trainer.getUser().setLastName(lastName);
        trainer.setSpecialization(trainingType);

        log.info("Updated trainer username={}", username);

        return trainer;
    }

    @Transactional
    public void changePassword(
            @NotBlank String username,
            @NotBlank String oldPassword,
            @NotBlank String newPassword
    ) {
        authenticationService.requireTrainerAuthentication(username, oldPassword);

        Trainer trainer = getByUsername(username);

        trainer.getUser().setPassword(newPassword);
        log.info("Changed password for trainer username={}", username);
    }

    @Transactional
    public void activate(@NotBlank String username, @NotBlank String password) {
        authenticationService.requireTrainerAuthentication(username,password);

        Trainer trainer = getByUsername(username);

        if (trainer.getUser().isActive()) {
            throw new IllegalStateException(
                    "Trainer is already active"
            );
        }

        trainer.getUser().setActive(true);
        log.info("Activated trainer username={}", username);
    }

    @Transactional
    public void deactivate(@NotBlank String username, @NotBlank String password) {
        authenticationService.requireTrainerAuthentication(username,password);

        Trainer trainer = getByUsername(username);

        if (!trainer.getUser().isActive()) {
            throw new IllegalStateException(
                    "Trainer is already inactive"
            );
        }

        trainer.getUser().setActive(false);
        log.info("Deactivated trainer username={}", username);
    }
    public List<Trainer> findNotAssignedToTrainee(
            @NotBlank String traineeUsername,
            @NotBlank String password
    ) {
        authenticationService.requireTraineeAuthentication(
                traineeUsername,
                password
        );

        log.debug(
                "Searching trainers not assigned to trainee username={}",
                traineeUsername
        );

        return trainerDao.findNotAssignedToTrainee(
                traineeUsername
        );
    }

    public Trainer findByUsername(@NotBlank String username, @NotBlank String password) {
        authenticationService.requireTrainerAuthentication(username,password);

        return getByUsername(username);
    }

    private Trainer getByUsername(String username) {
        return trainerDao.findByUsername(username)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Trainer with username " + username + " not found"
                        ));
    }

    public TrainerProfileResponse getProfile(
            @NotBlank String username,
            @NotBlank String password
    ) {
        return toProfileResponse(findByUsername(username, password));
    }

    private TrainerProfileResponse toProfileResponse(Trainer trainer) {
        List<TraineeSummary> trainees = trainer.getTrainees().stream()
                .map(trainee -> new TraineeSummary(
                        trainee.getUser().getUsername(),
                        trainee.getUser().getFirstName(),
                        trainee.getUser().getLastName()
                ))
                .sorted(Comparator.comparing(TraineeSummary::username))
                .toList();

        User user = trainer.getUser();

        return new TrainerProfileResponse(
                user.getFirstName(),
                user.getLastName(),
                trainer.getSpecialization().getTrainingTypeName(),
                user.isActive(),
                trainees
        );
    }

    @Transactional
    public TrainerUpdateResponse updateProfile(
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotNull Boolean isActive
    ) {
        Trainer trainer = findByUsername(username, password);
        User user = trainer.getUser();

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setActive(isActive);

        TrainerProfileResponse profile = toProfileResponse(trainer);

        log.info("Updated trainer profile username={}", username);

        return new TrainerUpdateResponse(
                user.getUsername(),
                profile.firstName(),
                profile.lastName(),
                profile.specialization(),
                profile.isActive(),
                profile.trainees()
        );
    }

    public List<TrainerSummary> getUnassignedTrainers(
            @NotBlank String traineeUsername,
            @NotBlank String password
    ) {
        return findNotAssignedToTrainee(traineeUsername, password)
                .stream()
                .map(trainer -> new TrainerSummary(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization().getTrainingTypeName()
                ))
                .toList();
    }
}
