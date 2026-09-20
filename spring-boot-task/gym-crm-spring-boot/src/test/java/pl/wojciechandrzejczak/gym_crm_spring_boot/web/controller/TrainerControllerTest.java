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
import pl.wojciechandrzejczak.gym_crm_spring_boot.controller.TrainerController;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerProfileResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerUpdateResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TrainerTrainingResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.exception.GlobalExceptionHandler;
import pl.wojciechandrzejczak.gym_crm_spring_boot.facade.GymFacade;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainer;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingType;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.User;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(MockitoExtension.class)
class TrainerControllerTest {

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
                .standaloneSetup(new TrainerController(gymFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }


    @Test
    void shouldRegisterTrainerAndReturnOnlyCredentials()
            throws Exception {

        User user = new User(
                "Adam",
                "Trener",
                "Adam.Trener",
                "test-password",
                true
        );

        TrainingType trainingType =
                new TrainingType(TrainingTypeName.FITNESS);

        Trainer trainer = new Trainer(
                trainingType,
                user
        );

        when(gymFacade.createTrainer(
                "Adam",
                "Trener",
                TrainingTypeName.FITNESS
        )).thenReturn(trainer);

        var response = mockMvc.perform(
                post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Adam",
                                  "lastName": "Trener",
                                  "specialization": "FITNESS"
                                }
                                """)
        ).andReturn().getResponse();

        assertEquals(201, response.getStatus());

        assertNotNull(response.getContentType());

        assertTrue(
                MediaType.APPLICATION_JSON.isCompatibleWith(
                        MediaType.parseMediaType(
                                response.getContentType()
                        )
                )
        );

        var body = jsonMapper.readTree(
                response.getContentAsString()
        );

        assertEquals(2, body.size());

        assertEquals(
                "Adam.Trener",
                body.get("username").asString()
        );

        assertEquals(
                "test-password",
                body.get("password").asString()
        );

        verify(gymFacade).createTrainer(
                "Adam",
                "Trener",
                TrainingTypeName.FITNESS
        );

        verifyNoMoreInteractions(gymFacade);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
            {
              "firstName": "",
              "lastName": "Trener",
              "specialization": "FITNESS"
            }
            """,
            """
            {
              "firstName": "   ",
              "lastName": "Trener",
              "specialization": "FITNESS"
            }
            """,
            """
            {
              "lastName": "Trener",
              "specialization": "FITNESS"
            }
            """,
            """
            {
              "firstName": "Adam",
              "lastName": "",
              "specialization": "FITNESS"
            }
            """,
            """
            {
              "firstName": "Adam",
              "specialization": "FITNESS"
            }
            """
    })
    void shouldRejectInvalidTrainerNames(String json)
            throws Exception {

        var response = mockMvc.perform(
                post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
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
                "Request validation failed",
                body.get("detail").asString()
        );

        assertTrue(body.has("errors"));
        assertFalse(body.get("errors").isEmpty());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void shouldRejectRegistrationWithoutSpecialization()
            throws Exception {

        var response = mockMvc.perform(
                post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Adam",
                                  "lastName": "Trener"
                                }
                                """)
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void shouldRejectInvalidSpecialization()
            throws Exception {

        var response = mockMvc.perform(
                post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Adam",
                                  "lastName": "Trener",
                                  "specialization": "INVALID_TYPE"
                                }
                                """)
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(gymFacade);
    }


    @Test
    void shouldReturnTrainerProfile() throws Exception {
        String username = "Adam.Trener";
        String password = "test-password";

        TrainerProfileResponse profile =
                new TrainerProfileResponse(
                        "Adam",
                        "Trener",
                        TrainingTypeName.FITNESS,
                        true,
                        List.of()
                );

        when(gymFacade.getTrainerProfile(
                username,
                password
        )).thenReturn(profile);

        var response = mockMvc.perform(
                get(
                        "/api/trainers/{username}",
                        username
                )
                        .header(
                                "Password",
                                password
                        )
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        var body = jsonMapper.readTree(
                response.getContentAsString()
        );

        assertEquals(
                "Adam",
                body.get("firstName").asString()
        );

        assertEquals(
                "Trener",
                body.get("lastName").asString()
        );

        assertEquals(
                "FITNESS",
                body.get("specialization").asString()
        );

        assertTrue(
                body.get("isActive").asBoolean()
        );

        assertTrue(
                body.get("trainees").isArray()
        );

        assertTrue(
                body.get("trainees").isEmpty()
        );

        verify(gymFacade).getTrainerProfile(
                username,
                password
        );

        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    void shouldUpdateTrainerProfile() throws Exception {
        String username = "Adam.Trener";
        String password = "test-password";

        TrainerUpdateResponse result =
                new TrainerUpdateResponse(
                        username,
                        "Adam",
                        "Nowak",
                        TrainingTypeName.FITNESS,
                        true,
                        List.of()
                );

        when(gymFacade.updateTrainerProfile(
                username,
                password,
                "Adam",
                "Nowak",
                true
        )).thenReturn(result);

        var response = mockMvc.perform(
                put(
                        "/api/trainers/{username}",
                        username
                )
                        .header(
                                "Password",
                                password
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("""
                                {
                                  "firstName": "Adam",
                                  "lastName": "Nowak",
                                  "isActive": true
                                }
                                """)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        var body = jsonMapper.readTree(
                response.getContentAsString()
        );

        assertEquals(
                username,
                body.get("username").asString()
        );

        assertEquals(
                "Adam",
                body.get("firstName").asString()
        );

        assertEquals(
                "Nowak",
                body.get("lastName").asString()
        );

        assertEquals(
                "FITNESS",
                body.get("specialization").asString()
        );

        assertTrue(
                body.get("isActive").asBoolean()
        );

        assertTrue(
                body.get("trainees").isArray()
        );

        verify(gymFacade).updateTrainerProfile(
                username,
                password,
                "Adam",
                "Nowak",
                true
        );

        verifyNoMoreInteractions(gymFacade);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
            {
              "firstName": "",
              "lastName": "Nowak",
              "isActive": true
            }
            """,
            """
            {
              "firstName": "Adam",
              "lastName": "",
              "isActive": true
            }
            """,
            """
            {
              "firstName": "Adam",
              "lastName": "Nowak"
            }
            """
    })
    void shouldRejectInvalidTrainerUpdate(String json)
            throws Exception {

        var response = mockMvc.perform(
                put(
                        "/api/trainers/{username}",
                        "Adam.Trener"
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

        verifyNoInteractions(gymFacade);
    }

    @Test
    void shouldActivateTrainer() throws Exception {
        String username = "Adam.Trener";
        String password = "test-password";

        var response = mockMvc.perform(
                patch(
                        "/api/trainers/{username}",
                        username
                )
                        .header(
                                "Password",
                                password
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("""
                                {
                                  "isActive": true
                                }
                                """)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        assertTrue(
                response.getContentAsString().isEmpty()
        );

        verify(gymFacade).activateTrainer(
                username,
                password
        );

        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    void shouldDeactivateTrainer() throws Exception {
        String username = "Adam.Trener";
        String password = "test-password";

        var response = mockMvc.perform(
                patch(
                        "/api/trainers/{username}",
                        username
                )
                        .header(
                                "Password",
                                password
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("""
                                {
                                  "isActive": false
                                }
                                """)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        assertTrue(
                response.getContentAsString().isEmpty()
        );

        verify(gymFacade).deactivateTrainer(
                username,
                password
        );

        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    void shouldRejectActivityChangeWithoutIsActive()
            throws Exception {

        var response = mockMvc.perform(
                patch(
                        "/api/trainers/{username}",
                        "Adam.Trener"
                )
                        .header(
                                "Password",
                                "test-password"
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content("{}")
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void shouldReturnTrainerTrainingsWithFilters()
            throws Exception {

        String username = "Adam.Trener";
        String authUsername = "Adam.Trener";
        String authPassword = "test-password";

        LocalDate fromDate =
                LocalDate.of(2026, 1, 1);

        LocalDate toDate =
                LocalDate.of(2026, 12, 31);

        List<TrainerTrainingResponse> trainings =
                List.of(
                        new TrainerTrainingResponse(
                                "Morning training",
                                LocalDate.of(2026, 5, 10),
                                TrainingTypeName.FITNESS,
                                60,
                                "Jan Testowy"
                        )
                );

        when(gymFacade.getTrainerTrainingList(
                authUsername,
                authPassword,
                username,
                fromDate,
                toDate,
                "Jan Testowy"
        )).thenReturn(trainings);

        var response = mockMvc.perform(
                get(
                        "/api/trainers/{username}/trainings",
                        username
                )
                        .header(
                                "Username",
                                authUsername
                        )
                        .header(
                                "Password",
                                authPassword
                        )
                        .param(
                                "fromDate",
                                "2026-01-01"
                        )
                        .param(
                                "toDate",
                                "2026-12-31"
                        )
                        .param(
                                "traineeName",
                                "Jan Testowy"
                        )
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        var body = jsonMapper.readTree(
                response.getContentAsString()
        );

        assertTrue(body.isArray());
        assertEquals(1, body.size());

        var training = body.get(0);

        assertEquals(
                "Morning training",
                training.get("trainingName").asString()
        );

        assertEquals(
                "2026-05-10",
                training.get("trainingDate").asString()
        );

        assertEquals(
                "FITNESS",
                training.get("trainingType").asString()
        );

        assertEquals(
                60,
                training.get("trainingDuration").asInt()
        );

        assertEquals(
                "Jan Testowy",
                training.get("traineeName").asString()
        );

        verify(gymFacade).getTrainerTrainingList(
                authUsername,
                authPassword,
                username,
                fromDate,
                toDate,
                "Jan Testowy"
        );

        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    void shouldReturnTrainerTrainingsWithoutOptionalFilters()
            throws Exception {

        String username = "Adam.Trener";
        String authUsername = "Adam.Trener";
        String authPassword = "test-password";

        when(gymFacade.getTrainerTrainingList(
                authUsername,
                authPassword,
                username,
                null,
                null,
                null
        )).thenReturn(List.of());

        var response = mockMvc.perform(
                get(
                        "/api/trainers/{username}/trainings",
                        username
                )
                        .header(
                                "Username",
                                authUsername
                        )
                        .header(
                                "Password",
                                authPassword
                        )
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        var body = jsonMapper.readTree(
                response.getContentAsString()
        );

        assertTrue(body.isArray());
        assertTrue(body.isEmpty());

        verify(gymFacade).getTrainerTrainingList(
                authUsername,
                authPassword,
                username,
                null,
                null,
                null
        );

        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    void shouldRejectInvalidTrainingDate()
            throws Exception {

        var response = mockMvc.perform(
                get(
                        "/api/trainers/{username}/trainings",
                        "Adam.Trener"
                )
                        .header(
                                "Username",
                                "Adam.Trener"
                        )
                        .header(
                                "Password",
                                "test-password"
                        )
                        .param(
                                "fromDate",
                                "not-a-date"
                        )
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(gymFacade);
    }
}