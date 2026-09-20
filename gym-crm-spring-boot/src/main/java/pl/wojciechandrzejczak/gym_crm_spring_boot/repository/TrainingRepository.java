package pl.wojciechandrzejczak.gym_crm_spring_boot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Training;

public interface TrainingRepository extends JpaRepository<Training, Long> {
}
