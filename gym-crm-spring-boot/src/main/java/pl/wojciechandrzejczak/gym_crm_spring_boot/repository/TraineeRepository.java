package pl.wojciechandrzejczak.gym_crm_spring_boot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainee;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, Long> {
}
