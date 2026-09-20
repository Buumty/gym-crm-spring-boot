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
import pl.wojciechandrzejczak.gym_crm_spring_boot.controller.TrainingController;
import pl.wojciechandrzejczak.gym_crm_spring_boot.exception.GlobalExceptionHandler;
import pl.wojciechandrzejczak.gym_crm_spring_boot.facade.GymFacade;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    @Mock
    private GymFacade gymFacade;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TrainingController(gymFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }


    @Test
    void shouldAddTraining() throws Exception {
        String authUsername = "Jan.Testowy";
        String authPassword = "test-password";

        LocalDate trainingDate =
                LocalDate.of(2026, 5, 10);

        var response = mockMvc.perform(
                post("/api/trainings")
                        .header(
                                "Username",
                                authUsername
                        )
                        .header(
                                "Password",
                                authPassword
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("""
                                {
                                  "traineeUsername": "Jan.Testowy",
                                  "trainerUsername": "Adam.Trener",
                                  "trainingName": "Morning training",
                                  "trainingDate": "2026-05-10",
                                  "trainingDuration": 60
                                }
                                """)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        assertTrue(
                response.getContentAsString().isEmpty()
        );

        verify(gymFacade).addTraining(
                authUsername,
                authPassword,
                "Jan.Testowy",
                "Adam.Trener",
                "Morning training",
                trainingDate,
                60
        );

        verifyNoMoreInteractions(gymFacade);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
            {
              "traineeUsername": "",
              "trainerUsername": "Adam.Trener",
              "trainingName": "Morning training",
              "trainingDate": "2026-05-10",
              "trainingDuration": 60
            }
            """,
            """
            {
              "traineeUsername": "Jan.Testowy",
              "trainerUsername": "",
              "trainingName": "Morning training",
              "trainingDate": "2026-05-10",
              "trainingDuration": 60
            }
            """,
            """
            {
              "traineeUsername": "Jan.Testowy",
              "trainerUsername": "Adam.Trener",
              "trainingName": "",
              "trainingDate": "2026-05-10",
              "trainingDuration": 60
            }
            """,
            """
            {
              "traineeUsername": "Jan.Testowy",
              "trainerUsername": "Adam.Trener",
              "trainingName": "Morning training",
              "trainingDuration": 60
            }
            """,
            """
            {
              "traineeUsername": "Jan.Testowy",
              "trainerUsername": "Adam.Trener",
              "trainingName": "Morning training",
              "trainingDate": "2026-05-10"
            }
            """
    })
    void shouldRejectMissingOrBlankRequiredFields(String json)
            throws Exception {

        var response = mockMvc.perform(
                post("/api/trainings")
                        .header(
                                "Username",
                                "Jan.Testowy"
                        )
                        .header(
                                "Password",
                                "test-password"
                        )
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

        assertEquals(
                "Validation failure",
                body.get("detail").asString()
        );

        verifyNoInteractions(gymFacade);
    }


    @ParameterizedTest
    @ValueSource(ints = {
            0,
            -1,
            -60
    })
    void shouldRejectNonPositiveTrainingDuration(int duration)
            throws Exception {

        var response = mockMvc.perform(
                post("/api/trainings")
                        .header(
                                "Username",
                                "Jan.Testowy"
                        )
                        .header(
                                "Password",
                                "test-password"
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("""
                                {
                                  "traineeUsername": "Jan.Testowy",
                                  "trainerUsername": "Adam.Trener",
                                  "trainingName": "Morning training",
                                  "trainingDate": "2026-05-10",
                                  "trainingDuration": %d
                                }
                                """.formatted(duration))
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        var body = jsonMapper.readTree(
                response.getContentAsString()
        );

        assertEquals(
                400,
                body.get("status").asInt()
        );

        assertEquals(
                "Validation failure",
                body.get("detail").asString()
        );


        verifyNoInteractions(gymFacade);
    }

    @Test
    void shouldRejectInvalidTrainingDate() throws Exception {

        var response = mockMvc.perform(
                post("/api/trainings")
                        .header(
                                "Username",
                                "Jan.Testowy"
                        )
                        .header(
                                "Password",
                                "test-password"
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("""
                                {
                                  "traineeUsername": "Jan.Testowy",
                                  "trainerUsername": "Adam.Trener",
                                  "trainingName": "Morning training",
                                  "trainingDate": "not-a-date",
                                  "trainingDuration": 60
                                }
                                """)
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(gymFacade);
    }
}