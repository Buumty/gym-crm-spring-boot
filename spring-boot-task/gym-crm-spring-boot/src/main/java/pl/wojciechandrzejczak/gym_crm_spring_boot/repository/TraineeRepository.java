package pl.wojciechandrzejczak.gym_crm_spring_boot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainee;

import java.util.Optional;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {
    Optional<Trainee> findByUser_Username(String username);
    boolean existsByUser_Username(String username);
}
