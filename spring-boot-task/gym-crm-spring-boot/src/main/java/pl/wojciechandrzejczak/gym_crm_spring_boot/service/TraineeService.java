package pl.wojciechandrzejczak.gym_crm_spring_boot.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainee.TraineeProfileResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainee.TraineeUpdateResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerSummary;
import pl.wojciechandrzejczak.gym_crm_spring_boot.metrics.GymMetrics;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainee;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainer;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.User;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TraineeRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainerRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication.AuthenticationService;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.generator.PasswordGenerator;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.generator.UsernameGenerator;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@Validated
@Transactional(readOnly = true)
public class TraineeService {
    private static final Logger log =
            LoggerFactory.getLogger(TraineeService.class);

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final PasswordGenerator passwordGenerator;
    private final UsernameGenerator usernameGenerator;
    private final AuthenticationService authenticationService;
    private final GymMetrics gymMetrics;


    public TraineeService(TraineeRepository traineeRepository, TrainerRepository trainerRepository, PasswordGenerator passwordGenerator, UsernameGenerator usernameGenerator, AuthenticationService authenticationService, GymMetrics gymMetrics) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.passwordGenerator = passwordGenerator;
        this.usernameGenerator = usernameGenerator;
        this.authenticationService = authenticationService;
        this.gymMetrics = gymMetrics;
    }

    public Trainee findById(long id, String username, String password) {
        authenticationService.requireTraineeAuthentication(username, password);
        log.debug("Searching for trainee with id={}", id);
        return traineeRepository.findById(id).orElseThrow(() -> new NoSuchElementException(
                "Trainee with id " + id + " not found"
        ));
    }

    public List<Trainee> findAll(String username, String password) {
        authenticationService.requireTraineeAuthentication(username, password);
        log.debug("Retrieving all trainees");
        return traineeRepository.findAll();
    }

    @Transactional
    public Trainee create(
            @NotBlank String firstName,
            @NotBlank String lastName,
            LocalDate dateOfBirth,
            String address
    ) {

        User user = new User(firstName,
                lastName,
                usernameGenerator.generate(firstName, lastName),
                passwordGenerator.generate(),
                true);

        Trainee trainee = new Trainee(user,
                dateOfBirth,
                address);


        Trainee savedTrainee = traineeRepository.save(trainee);
        gymMetrics.incrementTraineeRegistrations();

        log.info(
                "Created trainee id={}, username={}",
                savedTrainee.getTraineeId(),
                savedTrainee.getUser().getUsername()
        );

        return savedTrainee;
    }

    @Transactional
    public Trainee update(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String address,
            LocalDate dateOfBirth,
            @NotBlank String username,
            @NotBlank String password
    ) {
        authenticationService.requireTraineeAuthentication(username,password);
        Trainee trainee = getByUsername(username);

        trainee.getUser().setFirstName(firstName);
        trainee.getUser().setLastName(lastName);
        trainee.setAddress(address);
        trainee.setDateOfBirth(dateOfBirth);

        log.info("Updated trainee username={}", username);

        return trainee;
    }

    @Transactional
    public void deleteByUsername(
            String username,
            String password
    ) {
        authenticationService
                .requireTraineeAuthentication(username, password);

        Trainee trainee = getByUsername(username);

        traineeRepository.delete(trainee);

        log.info(
                "Deleted trainee username={}",
                username
        );
    }

    @Transactional
    public void changePassword(
            @NotBlank String username,
            @NotBlank String oldPassword,
            @NotBlank String newPassword
    ) {
        authenticationService
                .requireTraineeAuthentication(
                        username,
                        oldPassword
                );

        Trainee trainee = getByUsername(username);

        trainee.getUser().setPassword(newPassword);
    }

    @Transactional
    public void activate(String username, String password) {
        authenticationService.requireTraineeAuthentication(username,password);

        Trainee trainee = getByUsername(username);

        if (trainee.getUser().isActive()) {
            throw new IllegalStateException(
                    "Trainee is already active"
            );
        }
        trainee.getUser().setActive(true);
        log.info("Activated trainee username={}", username);
    }

    @Transactional
    public void deactivate(String username, String password) {
        authenticationService.requireTraineeAuthentication(username,password);

        Trainee trainee = getByUsername(username);

        if (!trainee.getUser().isActive()) {
            throw new IllegalStateException(
                    "Trainee is already inactive"
            );
        }

        trainee.getUser().setActive(false);
        log.info("Deactivated trainee username={}", username);
    }

    @Transactional
    public Trainee updateTrainers(
            String username,
            String password,
            Set<String> trainerUsernames
    ) {
        authenticationService.requireTraineeAuthentication(
                username,
                password
        );

        Trainee trainee = getByUsername(username);

        if (trainerUsernames == null) {
            throw new IllegalArgumentException(
                    "Trainer usernames cannot be null"
            );
        }

        if (trainerUsernames.isEmpty()) {
            trainee.getTrainers().clear();

            log.info(
                    "Removed all trainers from trainee username={}",
                    username
            );

            return trainee;
        }

        List<Trainer> trainers =
                trainerRepository.findByUser_UsernameIn(trainerUsernames);

        if (trainers.size() != trainerUsernames.size()) {
            throw new NoSuchElementException(
                    "One or more trainers were not found"
            );
        }

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);

        log.info(
                "Updated trainers list for trainee username={}",
                username
        );

        return trainee;
    }


    public Trainee findByUsername(String username, String password) {
        authenticationService.requireTraineeAuthentication(username, password);

        return getByUsername(username);


    }

    private Trainee getByUsername(String username) {
        return traineeRepository.findByUser_Username(username)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Trainee with username " + username + " not found"
                        ));
    }
    public TraineeProfileResponse getProfile(
            @NotBlank String username,
            @NotBlank String password
    ) {
        return toProfileResponse(findByUsername(username, password));
    }

    @Transactional
    public TraineeUpdateResponse updateProfile(
            @NotBlank String username,
            @NotBlank String password,
            @NotBlank String firstName,
            @NotBlank String lastName,
            LocalDate dateOfBirth,
            String address,
            @NotNull Boolean isActive
    ) {
        Trainee trainee = update(
                firstName,
                lastName,
                address,
                dateOfBirth,
                username,
                password
        );

        trainee.getUser().setActive(isActive);

        TraineeProfileResponse profile = toProfileResponse(trainee);

        return new TraineeUpdateResponse(
                trainee.getUser().getUsername(),
                profile.firstName(),
                profile.lastName(),
                profile.dateOfBirth(),
                profile.address(),
                profile.isActive(),
                profile.trainers()
        );
    }

    private TraineeProfileResponse toProfileResponse(Trainee trainee) {
        List<TrainerSummary> trainers = trainee.getTrainers().stream()
                .map(trainer -> new TrainerSummary(
                        trainer.getUser().getUsername(),
                        trainer.getUser().getFirstName(),
                        trainer.getUser().getLastName(),
                        trainer.getSpecialization().getTrainingTypeName()
                ))
                .sorted(Comparator.comparing(TrainerSummary::username))
                .toList();

        User user = trainee.getUser();

        return new TraineeProfileResponse(
                user.getFirstName(),
                user.getLastName(),
                trainee.getDateOfBirth(),
                trainee.getAddress(),
                user.isActive(),
                trainers
        );
    }

    @Transactional
    public List<TrainerSummary> updateTrainersList(
            @NotBlank String username,
            @NotBlank String password,
            @NotNull Set<@NotBlank String> trainerUsernames
    ) {
        Trainee trainee = updateTrainers(
                username,
                password,
                trainerUsernames
        );

        return toProfileResponse(trainee).trainers();
    }
}
