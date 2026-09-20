package pl.wojciechandrzejczak.gym_crm_spring_boot.service;

import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TrainingTypeResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.repository.TrainingTypeRepository;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication.AuthenticationService;

import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;
    private final AuthenticationService authenticationService;

    public TrainingTypeService(
            TrainingTypeRepository trainingTypeRepository,
            AuthenticationService authenticationService
    ) {
        this.trainingTypeRepository = trainingTypeRepository;
        this.authenticationService = authenticationService;
    }

    public List<TrainingTypeResponse> findAll(
            @NotBlank String username,
            @NotBlank String password
    ) {
        authenticationService.requireAuthentication(username, password);

        return trainingTypeRepository.findAll().stream()
                .map(type -> new TrainingTypeResponse(
                        type.getTrainingTypeId(),
                        type.getTrainingTypeName()
                ))
                .toList();
    }
}