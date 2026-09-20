package pl.wojciechandrzejczak.gym_crm_spring_boot.web.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import pl.wojciechandrzejczak.gym_crm_spring_boot.controller.AuthenticationController;
import pl.wojciechandrzejczak.gym_crm_spring_boot.exception.GlobalExceptionHandler;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication.AuthenticationService;
import pl.wojciechandrzejczak.gym_crm_spring_boot.service.authentication.PasswordService;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private PasswordService passwordService;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new AuthenticationController(
                                authenticationService,
                                passwordService
                        )
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }


    @Test
    void shouldLoginWithValidCredentials() throws Exception {
        String username = "Jan.Testowy";
        String password = "test-password";

        var response = mockMvc.perform(
                get("/api/login")
                        .header(
                                "Username",
                                username
                        )
                        .header(
                                "Password",
                                password
                        )
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        assertTrue(
                response.getContentAsString().isEmpty()
        );

        verify(authenticationService)
                .requireAuthentication(
                        username,
                        password
                );

        verifyNoInteractions(passwordService);

        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    void shouldRejectLoginWithoutUsernameHeader()
            throws Exception {

        var response = mockMvc.perform(
                get("/api/login")
                        .header(
                                "Password",
                                "test-password"
                        )
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(authenticationService);
        verifyNoInteractions(passwordService);
    }

    @Test
    void shouldRejectLoginWithoutPasswordHeader()
            throws Exception {

        var response = mockMvc.perform(
                get("/api/login")
                        .header(
                                "Username",
                                "Jan.Testowy"
                        )
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(authenticationService);
        verifyNoInteractions(passwordService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   "
    })
    void shouldRejectLoginWithBlankUsername(String username)
            throws Exception {

        var response = mockMvc.perform(
                get("/api/login")
                        .header(
                                "Username",
                                username
                        )
                        .header(
                                "Password",
                                "test-password"
                        )
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(authenticationService);
        verifyNoInteractions(passwordService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   "
    })
    void shouldRejectLoginWithBlankPassword(String password)
            throws Exception {

        var response = mockMvc.perform(
                get("/api/login")
                        .header(
                                "Username",
                                "Jan.Testowy"
                        )
                        .header(
                                "Password",
                                password
                        )
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(authenticationService);
        verifyNoInteractions(passwordService);
    }


    @Test
    void shouldChangePassword() throws Exception {
        String username = "Jan.Testowy";
        String oldPassword = "old-password";
        String newPassword = "new-password";

        var response = mockMvc.perform(
                put("/api/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("""
                                {
                                  "username": "Jan.Testowy",
                                  "oldPassword": "old-password",
                                  "newPassword": "new-password"
                                }
                                """)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        assertTrue(
                response.getContentAsString().isEmpty()
        );

        verify(passwordService).changePassword(
                username,
                oldPassword,
                newPassword
        );

        verifyNoInteractions(authenticationService);

        verifyNoMoreInteractions(passwordService);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
            {
              "username": "",
              "oldPassword": "old-password",
              "newPassword": "new-password"
            }
            """,
            """
            {
              "username": "   ",
              "oldPassword": "old-password",
              "newPassword": "new-password"
            }
            """,
            """
            {
              "oldPassword": "old-password",
              "newPassword": "new-password"
            }
            """,
            """
            {
              "username": "Jan.Testowy",
              "oldPassword": "",
              "newPassword": "new-password"
            }
            """,
            """
            {
              "username": "Jan.Testowy",
              "newPassword": "new-password"
            }
            """,
            """
            {
              "username": "Jan.Testowy",
              "oldPassword": "old-password",
              "newPassword": ""
            }
            """,
            """
            {
              "username": "Jan.Testowy",
              "oldPassword": "old-password"
            }
            """
    })
    void shouldRejectInvalidChangePasswordRequest(String json)
            throws Exception {

        var response = mockMvc.perform(
                put("/api/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(json)
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        assertNotNull(response.getContentType());

        assertTrue(
                MediaType.APPLICATION_PROBLEM_JSON.isCompatibleWith(
                        MediaType.parseMediaType(
                                response.getContentType()
                        )
                )
        );

        var body = jsonMapper.readTree(
                response.getContentAsString()
        );

        assertEquals(
                400,
                body.get("status").asInt()
        );

        verifyNoInteractions(passwordService);
        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldRejectMalformedChangePasswordRequest()
            throws Exception {

        var response = mockMvc.perform(
                put("/api/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("""
                                {
                                  "username": "Jan.Testowy",
                                  "oldPassword":
                                }
                                """)
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(passwordService);
        verifyNoInteractions(authenticationService);
    }

    @Test
    void shouldRejectMissingChangePasswordRequestBody()
            throws Exception {

        var response = mockMvc.perform(
                put("/api/login")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(passwordService);
        verifyNoInteractions(authenticationService);
    }
}