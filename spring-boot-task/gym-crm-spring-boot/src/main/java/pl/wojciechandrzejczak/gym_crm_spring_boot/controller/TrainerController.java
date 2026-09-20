package pl.wojciechandrzejczak.gym_crm_spring_boot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerProfileResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerRegistrationRequest;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerUpdateRequest;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerUpdateResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TrainerTrainingResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.user.ActivationRequest;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.user.RegistrationResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.facade.GymFacade;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainer;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@Tag(
        name = "Trainers",
        description = "Trainer registration, profiles and trainings"
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
        responseCode = "500",
        description = "Unexpected server error",
        content = @Content(
                mediaType = "application/problem+json",
                schema = @Schema(implementation = ProblemDetail.class)
        )
)
public class TrainerController {

    private static final Logger log =
            LoggerFactory.getLogger(TrainerController.class);

    private final GymFacade gymFacade;

    public TrainerController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @PostMapping
    @Operation(
            summary = "Register a trainer",
            description = """
                Creates an active trainer profile.
                First name, last name and specialization are required.
                Generates username and password.
                Authentication is not required.
                """
    )
    @ApiResponse(
            responseCode = "201",
            description = "Trainer registered",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegistrationResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "The specified training type was not found in the database",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TrainerRegistrationRequest request
    ) {
        log.info(
                "Register trainer request specialization={}",
                request.specialization()
        );

        Trainer trainer = gymFacade.createTrainer(
                request.firstName(),
                request.lastName(),
                request.specialization()
        );

        RegistrationResponse response = new RegistrationResponse(
                trainer.getUser().getUsername(),
                trainer.getUser().getPassword()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    @Operation(
            summary = "Get trainer profile",
            description = """
                Returns the profile, specialization and assigned trainees.
                Requires the password of the trainer identified
                by the path username in the Password header.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Trainer profile",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TrainerProfileResponse.class)
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
    public ResponseEntity<TrainerProfileResponse> getProfile(
            @PathVariable("username") @NotBlank String username,
            @RequestHeader("Password") @NotBlank String password
    ) {
        return ResponseEntity.ok(
                gymFacade.getTrainerProfile(username, password)
        );
    }
    @PutMapping("/{username}")
    @Operation(
            summary = "Update trainer profile",
            description = """
                Updates first name, last name and isActive.
                All three fields are required.
                Username and specialization remain unchanged.
                Requires the trainer's password in the Password header.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Updated trainer profile",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TrainerUpdateResponse.class)
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
    public ResponseEntity<TrainerUpdateResponse> updateProfile(
            @PathVariable("username") @NotBlank String username,
            @RequestHeader("Password") @NotBlank String password,
            @Valid @RequestBody TrainerUpdateRequest request
    ) {
        log.info(
                "Update trainer profile request username={} isActive={}",
                username,
                request.isActive()
        );

        return ResponseEntity.ok(
                gymFacade.updateTrainerProfile(
                        username,
                        password,
                        request.firstName(),
                        request.lastName(),
                        request.isActive()
                )
        );
    }

    @PatchMapping("/{username}")
    @Operation(
            summary = "Activate or deactivate a trainer",
            description = """
                Sets the requested activity state.
                Requesting the current state returns 409.
                Requires the trainer's password in the Password header.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Activity changed",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "Trainer already has the requested activity state",
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
    public ResponseEntity<Void> changeActivity(
            @PathVariable("username") @NotBlank String username,
            @RequestHeader("Password") @NotBlank String password,
            @Valid @RequestBody ActivationRequest request
    ) {
        log.info(
                "Change trainer activity request username={} isActive={}",
                username,
                request.isActive()
        );

        if (request.isActive()) {
            gymFacade.activateTrainer(username, password);
        } else {
            gymFacade.deactivateTrainer(username, password);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/trainings")
    @Operation(
            summary = "Get trainer trainings",
            description = """
                Returns trainings for the trainer identified by the path username.
                Optional filters: fromDate, toDate and traineeName.
                Dates use yyyy-MM-dd and both boundaries are inclusive.
                Trainee name matches first name, last name or full name,
                ignoring case. From date must not be after to date.
                Requires valid trainee or trainer credentials
                in Username and Password headers.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Matching trainings; the list may be empty",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(
                                    implementation = TrainerTrainingResponse.class
                            )
                    )
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
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainings(
            @PathVariable("username") @NotBlank String username,
            @RequestHeader("Username") @NotBlank String authUsername,
            @RequestHeader("Password") @NotBlank String authPassword,
            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @RequestParam(name = "traineeName", required = false)
            String traineeName
    ) {
        log.info(
                "Search trainer trainings request username={} fromDate={} "
                        + "toDate={} traineeName={}",
                username,
                fromDate,
                toDate,
                traineeName
        );

        return ResponseEntity.ok(
                gymFacade.getTrainerTrainingList(
                        authUsername,
                        authPassword,
                        username,
                        fromDate,
                        toDate,
                        traineeName
                )
        );
    }
}