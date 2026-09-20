package pl.wojciechandrzejczak.gym_crm_spring_boot.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.*;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TrainingRepositoryTest {

    @Autowired
    private TrainingRepository trainingRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        TrainingType strength =
                new TrainingType(TrainingTypeName.STRENGTH);

        TrainingType cardio =
                new TrainingType(TrainingTypeName.CARDIO);

        entityManager.persist(strength);
        entityManager.persist(cardio);

        Trainee john = new Trainee(
                new User(
                        "John", "Smith", "John.Smith",
                        "Password123", true
                ),
                LocalDate.of(1995, 5, 10),
                "Warsaw"
        );

        Trainee mike = new Trainee(
                new User(
                        "Mike", "Jones", "Mike.Jones",
                        "Password123", true
                ),
                LocalDate.of(1990, 1, 1),
                "Krakow"
        );

        Trainer anna = new Trainer(
                strength,
                new User(
                        "Anna", "Brown", "Anna.Brown",
                        "Password123", true
                )
        );

        Trainer bob = new Trainer(
                cardio,
                new User(
                        "Bob", "White", "Bob.White",
                        "Password123", true
                )
        );

        entityManager.persist(john);
        entityManager.persist(mike);
        entityManager.persist(anna);
        entityManager.persist(bob);

        entityManager.persist(new Training(
                john, anna, "John strength", strength,
                LocalDate.of(2026, 8, 10), 60
        ));

        entityManager.persist(new Training(
                john, bob, "John cardio", cardio,
                LocalDate.of(2026, 8, 20), 45
        ));

        entityManager.persist(new Training(
                mike, anna, "Mike strength", strength,
                LocalDate.of(2026, 8, 15), 60
        ));

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void shouldReturnOnlySelectedTraineeTrainingsWithoutFilters() {
        var result = trainingRepository.findTraineeTrainings(
                "John.Smith",
                null,
                null,
                null,
                null
        );

        assertThat(result)
                .extracting(Training::getTrainingName)
                .containsExactlyInAnyOrder(
                        "John strength",
                        "John cardio"
                );
    }

    @Test
    void shouldReturnOnlySelectedTrainerTrainingsWithoutFilters() {
        var result = trainingRepository.findTrainerTrainings(
                "Anna.Brown",
                null,
                null,
                null
        );

        assertThat(result)
                .extracting(Training::getTrainingName)
                .containsExactlyInAnyOrder(
                        "John strength",
                        "Mike strength"
                );
    }

    @Test
    void shouldFilterTraineeTrainingsByType() {
        var result = trainingRepository.findTraineeTrainings(
                "John.Smith",
                null,
                null,
                null,
                TrainingTypeName.CARDIO
        );

        assertThat(result)
                .extracting(Training::getTrainingName)
                .containsExactly("John cardio");
    }

    @Test
    void shouldFilterTraineeTrainingsByTrainerNameIgnoringCase() {
        var result = trainingRepository.findTraineeTrainings(
                "John.Smith",
                null,
                null,
                "aNnA bRoWn",
                null
        );

        assertThat(result)
                .extracting(Training::getTrainingName)
                .containsExactly("John strength");
    }

    @Test
    void shouldFilterTrainerTrainingsByTraineeNameIgnoringCase() {
        var result = trainingRepository.findTrainerTrainings(
                "Anna.Brown",
                null,
                null,
                "jOhN sMiTh"
        );

        assertThat(result)
                .extracting(Training::getTrainingName)
                .containsExactly("John strength");
    }

    @Test
    void shouldIncludeBothDateBoundariesForTrainee() {
        var result = trainingRepository.findTraineeTrainings(
                "John.Smith",
                LocalDate.of(2026, 8, 10),
                LocalDate.of(2026, 8, 10),
                null,
                null
        );

        assertThat(result)
                .extracting(Training::getTrainingName)
                .containsExactly("John strength");
    }

    @Test
    void shouldIncludeBothDateBoundariesForTrainer() {
        var result = trainingRepository.findTrainerTrainings(
                "Anna.Brown",
                LocalDate.of(2026, 8, 15),
                LocalDate.of(2026, 8, 15),
                null
        );

        assertThat(result)
                .extracting(Training::getTrainingName)
                .containsExactly("Mike strength");
    }
}