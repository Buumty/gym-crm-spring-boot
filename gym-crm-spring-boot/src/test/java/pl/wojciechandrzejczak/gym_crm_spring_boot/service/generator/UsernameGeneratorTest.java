package pl.wojciechandrzejczak.gym_crm_spring_boot.service.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TraineeRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainerRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @InjectMocks
    private UsernameGenerator usernameGenerator;

    @Test
    void shouldGenerateUsernameFromFirstAndLastName() {
        when(traineeRepository.existsByUser_Username("John.Smith"))
                .thenReturn(false);

        when(trainerRepository.existsByUser_Username("John.Smith"))
                .thenReturn(false);

        String username = usernameGenerator.generate(
                "John",
                "Smith"
        );

        assertEquals("John.Smith", username);
    }

    @Test
    void shouldAddSuffixWhenUsernameExistsForTrainee() {
        when(traineeRepository.existsByUser_Username("John.Smith"))
                .thenReturn(true);

        when(traineeRepository.existsByUser_Username("John.Smith1"))
                .thenReturn(false);

        when(trainerRepository.existsByUser_Username("John.Smith1"))
                .thenReturn(false);

        String username = usernameGenerator.generate(
                "John",
                "Smith"
        );

        assertEquals("John.Smith1", username);
    }

    @Test
    void shouldAddSuffixWhenUsernameExistsForTrainer() {
        when(traineeRepository.existsByUser_Username("John.Smith"))
                .thenReturn(false);

        when(trainerRepository.existsByUser_Username("John.Smith"))
                .thenReturn(true);

        when(traineeRepository.existsByUser_Username("John.Smith1"))
                .thenReturn(false);

        when(trainerRepository.existsByUser_Username("John.Smith1"))
                .thenReturn(false);

        String username = usernameGenerator.generate(
                "John",
                "Smith"
        );

        assertEquals("John.Smith1", username);
    }

    @Test
    void shouldIncrementSuffixUntilUsernameIsUnique() {
        when(traineeRepository.existsByUser_Username("John.Smith"))
                .thenReturn(true);

        when(traineeRepository.existsByUser_Username("John.Smith1"))
                .thenReturn(true);

        when(traineeRepository.existsByUser_Username("John.Smith2"))
                .thenReturn(false);

        when(trainerRepository.existsByUser_Username("John.Smith2"))
                .thenReturn(false);

        String username = usernameGenerator.generate(
                "John",
                "Smith"
        );

        assertEquals("John.Smith2", username);
    }
}