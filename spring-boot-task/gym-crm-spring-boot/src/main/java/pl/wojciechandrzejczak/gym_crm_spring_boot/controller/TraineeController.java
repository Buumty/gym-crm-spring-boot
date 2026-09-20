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
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainee.*;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerSummary;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TraineeTrainingResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.user.ActivationRequest;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.user.RegistrationResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.facade.GymFacade;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainee;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainees")
@Tag(
        name = "Trainees",
        description = "Trainee registration, profiles and trainer assignments"
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
public class TraineeController {

    private static final Logger log =
            LoggerFactory.getLogger(TraineeController.class);

    private final GymFacade gymFacade;

    public TraineeController(GymFacade gymFacade) {
        this.gymFacade = gymFacade;
    }

    @PostMapping
    @Operation(
            summary = "Register a trainee",
            description = """
                Creates an active trainee profile.
                First name and last name are required.
                Date of birth and address are optional.
                Generates username and password. Authentication is not required.
                """
    )
    @ApiResponse(
            responseCode = "201",
            description = "Trainee registered",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegistrationResponse.class)
            )
    )
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody TraineeRegistrationRequest request
    ) {
        log.info(
                "Register trainee request dateOfBirthProvided={} addressProvided={}",
                request.dateOfBirth() != null,
                request.address() != null && !request.address().isBlank()
        );

        Trainee trainee = gymFacade.createTrainee(
                request.firstName(),
                request.lastName(),
                request.dateOfBirth(),
                request.address()
        );

        RegistrationResponse response = new RegistrationResponse(
                trainee.getUser().getUsername(),
                trainee.getUser().getPassword()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{username}")
    @Operation(
            summary = "Get trainee profile",
            description = """
                Returns the profile and assigned trainers.
                The Password header must contain the password
                of the trainee identified by the path username.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Trainee profile",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TraineeProfileResponse.class)
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
    public ResponseEntity<TraineeProfileResponse> getProfile(
            @PathVariable("username") @NotBlank String username,
            @RequestHeader("Password") @NotBlank String password
    ) {
        return ResponseEntity.ok(
                gymFacade.getTraineeProfile(username, password)
        );
    }

    @PutMapping("/{username}")
    @Operation(
            summary = "Update trainee profile",
            description = """
                Updates first name, last name, date of birth, address and activity.
                First name, last name and isActive are required.
                Omitted date of birth and address are cleared.
                Username remains unchanged.
                Requires the trainee's password in the Password header.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Updated trainee profile",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TraineeUpdateResponse.class)
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
    public ResponseEntity<TraineeUpdateResponse> updateProfile(
            @PathVariable("username") @NotBlank String username,
            @RequestHeader("Password") @NotBlank String password,
            @Valid @RequestBody TraineeUpdateRequest request
    ) {
        log.info(
                "Update trainee profile request username={} isActive={} "
                        + "dateOfBirthProvided={} addressProvided={}",
                username,
                request.isActive(),
                request.dateOfBirth() != null,
                request.address() != null && !request.address().isBlank()
        );

        return ResponseEntity.ok(
                gymFacade.updateTraineeProfile(
                        username,
                        password,
                        request.firstName(),
                        request.lastName(),
                        request.dateOfBirth(),
                        request.address(),
                        request.isActive()
                )
        );
    }

    @DeleteMapping("/{username}")
    @Operation(
            summary = "Delete trainee profile",
            description = """
                Permanently deletes the trainee, associated user and their trainings.
                Removes trainer assignments without deleting trainers.
                Requires the trainee's password in the Password header.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Trainee deleted",
            content = @Content
    )
    @ApiResponse(
            responseCode = "401",
            description = "Invalid credentials",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    public ResponseEntity<Void> deleteProfile(
            @PathVariable("username") @NotBlank String username,
            @RequestHeader("Password") @NotBlank String password
    ) {
        gymFacade.deleteTrainee(username, password);

        return ResponseEntity.ok().build();
    }
    @PatchMapping("/{username}")
    @Operation(
            summary = "Activate or deactivate a trainee",
            description = """
                Sets the requested activity state.
                Requesting the current state returns 409.
                Requires the trainee's password in the Password header.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Activity changed",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "Trainee already has the requested activity state",
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
                "Change trainee activity request username={} isActive={}",
                username,
                request.isActive()
        );

        if (request.isActive()) {
            gymFacade.activateTrainee(username, password);
        } else {
            gymFacade.deactivateTrainee(username, password);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{username}/unassigned-trainers")
    @Operation(
            summary = "Get active trainers not assigned to a trainee",
            description = """
                Returns active trainers who are not assigned to this trainee.
                Trainers assigned to other trainees may appear in the result.
                Requires the trainee's password in the Password header.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Available trainers; the list may be empty",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = TrainerSummary.class)
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
    public ResponseEntity<List<TrainerSummary>> getUnassignedTrainers(
            @PathVariable("username") @NotBlank String username,
            @RequestHeader("Password") @NotBlank String password
    ) {
        return ResponseEntity.ok(
                gymFacade.getUnassignedTrainers(username, password)
        );
    }

    @PutMapping("/{username}/trainers")
    @Operation(
            summary = "Replace trainee trainer assignments",
            description = """
                Replaces the entire trainer list with the supplied usernames.
                An empty list clears all assignments.
                If any trainer does not exist, assignments remain unchanged.
                Requires the trainee's password in the Password header.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Updated trainer list",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                            schema = @Schema(implementation = TrainerSummary.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "One or more trainers were not found",
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
    public ResponseEntity<List<TrainerSummary>> updateTrainers(
            @PathVariable("username") @NotBlank String username,
            @RequestHeader("Password") @NotBlank String password,
            @Valid @RequestBody TraineeTrainersUpdateRequest request
    ) {
        log.info(
                "Update trainee trainers request username={} trainerCount={}",
                username,
                request.trainerUsernames().size()
        );

        return ResponseEntity.ok(
                gymFacade.updateTraineeTrainersList(
                        username,
                        password,
                        request.trainerUsernames()
                )
        );
    }

    @GetMapping("/{username}/trainings")
    @Operation(
            summary = "Get trainee trainings",
            description = """
                Returns trainings for the trainee identified by the path username.
                Optional filters: fromDate, toDate, trainerName and trainingType.
                Dates use yyyy-MM-dd and both boundaries are inclusive.
                Trainer name matches first name, last name or full name,
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
                                    implementation = TraineeTrainingResponse.class
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
    public ResponseEntity<List<TraineeTrainingResponse>> getTrainings(
            @PathVariable("username") @NotBlank String username,
            @RequestHeader("Username") @NotBlank String authUsername,
            @RequestHeader("Password") @NotBlank String authPassword,
            @RequestParam(name = "fromDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(name = "toDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            @RequestParam(name = "trainerName", required = false)
            String trainerName,
            @RequestParam(name = "trainingType", required = false)
            TrainingTypeName trainingType
    ) {
        log.info(
                "Search trainee trainings request username={} fromDate={} "
                        + "toDate={} trainerName={} trainingType={}",
                username,
                fromDate,
                toDate,
                trainerName,
                trainingType
        );

        return ResponseEntity.ok(
                gymFacade.getTraineeTrainingList(
                        authUsername,
                        authPassword,
                        username,
                        fromDate,
                        toDate,
                        trainerName,
                        trainingType
                )
        );
    }
}