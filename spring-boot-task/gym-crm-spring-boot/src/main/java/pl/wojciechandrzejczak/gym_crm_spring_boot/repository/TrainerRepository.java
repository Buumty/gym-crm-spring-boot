package pl.wojciechandrzejczak.gym_crm_spring_boot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUser_Username(String username);
    boolean existsByUser_Username(String username);
    List<Trainer> findByUser_UsernameIn(Set<String> usernames);
    @Query("""
    SELECT tr
    FROM Trainer tr
    JOIN FETCH tr.user u
    JOIN FETCH tr.specialization
    WHERE u.isActive = true
      AND tr NOT IN (
          SELECT assignedTrainer
          FROM Trainee t
          JOIN t.trainers assignedTrainer
          WHERE t.user.username = :traineeUsername
      )
    ORDER BY u.username
    """)
    List<Trainer> findNotAssignedToTrainee(
            @Param("traineeUsername") String traineeUsername
    );
}
