package org.example.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.example.dto.training.TrainingCreateRequest;
import org.example.facade.GymFacade;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/trainings")
@Tag(
        name = "Trainings",
        description = "Training creation"
)
public class TrainingController {

    private static final Logger log =
            LoggerFactory.getLogger(TrainingController.class);

    private final GymFacade gymFacade;

    public TrainingController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @PostMapping
    @Operation(
            summary = "Add a training",
            description = """
                Creates a training for the specified trainee and trainer.
                All request fields are required. Duration must be positive.
                Training date uses yyyy-MM-dd.
                Training type is taken from the trainer's specialization.
                Does not modify trainee trainer assignments.
                Requires valid trainee or trainer credentials
                in Username and Password headers.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Training created",
            content = @Content
    )
    @ApiResponse(
            responseCode = "400",
            description = "Missing or invalid request data",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Trainee, trainer or training type was not found",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "Unexpected server error",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    public ResponseEntity<Void> addTraining(
            @RequestHeader("Username") @NotBlank String authUsername,
            @RequestHeader("Password") @NotBlank String authPassword,
            @Valid @RequestBody TrainingCreateRequest request
    ) {
        log.info(
                "Add training request traineeUsername={} trainerUsername={} "
                        + "date={} duration={}",
                request.traineeUsername(),
                request.trainerUsername(),
                request.trainingDate(),
                request.trainingDuration()
        );

        gymFacade.addTraining(
                authUsername,
                authPassword,
                request.traineeUsername(),
                request.trainerUsername(),
                request.trainingName(),
                request.trainingDate(),
                request.trainingDuration()
        );

        return ResponseEntity.ok().build();
    }
}