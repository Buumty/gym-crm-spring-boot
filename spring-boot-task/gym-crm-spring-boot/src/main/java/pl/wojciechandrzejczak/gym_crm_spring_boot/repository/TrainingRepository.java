package pl.wojciechandrzejczak.gym_crm_spring_boot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Training;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;

import java.time.LocalDate;
import java.util.List;

public interface TrainingRepository extends JpaRepository<Training, Long> {
    @Query("""
    SELECT t
    FROM Training t
    JOIN FETCH t.trainingType tt
    JOIN FETCH t.trainer trainer
    JOIN FETCH trainer.user u
    WHERE t.trainee.user.username = :traineeUsername
      AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)
      AND (:toDate IS NULL OR t.trainingDate <= :toDate)
      AND (:trainingType IS NULL OR tt.trainingTypeName = :trainingType)
      AND (
          :trainerName IS NULL
          OR LOWER(u.firstName) = LOWER(:trainerName)
          OR LOWER(u.lastName) = LOWER(:trainerName)
          OR LOWER(CONCAT(CONCAT(u.firstName, ' '), u.lastName))
             = LOWER(:trainerName)
      )
    """)
    List<Training> findTraineeTrainings(
            @Param("traineeUsername") String traineeUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("trainerName") String trainerName,
            @Param("trainingType") TrainingTypeName trainingType
    );

    @Query("""
    SELECT t
    FROM Training t
    JOIN FETCH t.trainingType
    JOIN FETCH t.trainee trainee
    JOIN FETCH trainee.user u
    WHERE t.trainer.user.username = :trainerUsername
      AND (:fromDate IS NULL OR t.trainingDate >= :fromDate)
      AND (:toDate IS NULL OR t.trainingDate <= :toDate)
      AND (
          :traineeName IS NULL
          OR LOWER(u.firstName) = LOWER(:traineeName)
          OR LOWER(u.lastName) = LOWER(:traineeName)
          OR LOWER(CONCAT(CONCAT(u.firstName, ' '), u.lastName))
             = LOWER(:traineeName)
      )
    """)
    List<Training> findTrainerTrainings(
            @Param("trainerUsername") String trainerUsername,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("traineeName") String traineeName
    );
}
