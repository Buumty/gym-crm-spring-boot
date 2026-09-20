package pl.wojciechandrzejczak.gym_crm_spring_boot.service;

import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TrainingTypeResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication.AuthenticationService;

import java.util.List;

@Service
@Validated
@Transactional(readOnly = true)
public class TrainingTypeService {

    private final TrainingTypeDao trainingTypeDao;
    private final AuthenticationService authenticationService;

    public TrainingTypeService(
            TrainingTypeDao trainingTypeDao,
            AuthenticationService authenticationService
    ) {
        this.trainingTypeDao = trainingTypeDao;
        this.authenticationService = authenticationService;
    }

    public List<TrainingTypeResponse> findAll(
            @NotBlank String username,
            @NotBlank String password
    ) {
        authenticationService.requireAuthentication(username, password);

        return trainingTypeDao.findAll().stream()
                .map(type -> new TrainingTypeResponse(
                        type.getTrainingTypeId(),
                        type.getTrainingTypeName()
                ))
                .toList();
    }
}