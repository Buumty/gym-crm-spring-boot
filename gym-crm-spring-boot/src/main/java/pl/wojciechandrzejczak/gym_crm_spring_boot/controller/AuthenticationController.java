package org.example.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.example.service.authentication.PasswordService;
import org.example.service.authentication.AuthenticationService;
import org.example.dto.user.ChangePasswordRequest;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Tag(
        name = "Authentication",
        description = "Credential verification and password changes"
)
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final PasswordService passwordService;

    public AuthenticationController(
            AuthenticationService authenticationService,
            PasswordService passwordService
    ) {
        this.authenticationService = authenticationService;
        this.passwordService = passwordService;
    }

    @GetMapping("/login")
    @Operation(
            summary = "Verify user credentials",
            description = """
                Accepts trainee or trainer credentials in Username and Password
                headers. Does not create a session or issue a token.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Credentials are valid",
            content = @Content
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
    public ResponseEntity<Void> login(
            @RequestHeader("Username") @NotBlank String username,
            @RequestHeader("Password") @NotBlank String password
    ) {
        authenticationService.requireAuthentication(username, password);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/login")
    @Operation(
            summary = "Change user password",
            description = """
                Changes the password of a trainee or trainer after verifying
                username and oldPassword from the request body.
                The username remains unchanged.
                """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Password changed successfully",
            content = @Content
    )
    @ApiResponse(
            responseCode = "400",
            description = "Request body is missing, malformed or contains blank fields",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ApiResponse(
            responseCode = "401",
            description = "Invalid username or old password",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        passwordService.changePassword(
                request.username(),
                request.oldPassword(),
                request.newPassword()
        );

        return ResponseEntity.ok().build();
    }
}