package org.example.service;

import jakarta.validation.constraints.NotBlank;
import org.example.dao.TrainingTypeDao;
import org.example.dto.training.TrainingTypeResponse;
import org.example.service.authentication.AuthenticationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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