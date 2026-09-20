package org.example.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.example.dto.training.TrainingTypeResponse;
import org.example.facade.GymFacade;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/training-types")
@Tag(
        name = "Training types",
        description = "Read-only training type dictionary"
)
public class TrainingTypeController {

    private final GymFacade gymFacade;

    public TrainingTypeController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @GetMapping
    @Operation(
            summary = "Get training types",
            description = """
                Returns the fixed training type dictionary with database identifiers.
                Requires valid trainee or trainer credentials
                in Username and Password headers.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Training types",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(
                                    implementation = TrainingTypeResponse.class
                            )
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "A required header is missing or blank",
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
            responseCode = "500",
            description = "Unexpected server error",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes(
            @RequestHeader("Username") @NotBlank String username,
            @RequestHeader("Password") @NotBlank String password
    ) {
        return ResponseEntity.ok(
                gymFacade.getTrainingTypes(username, password)
        );
    }
}