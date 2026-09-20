package pl.wojciechandrzejczak.gym_crm_spring_boot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainer;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
}
