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
import pl.wojciechandrzejczak.gym_crm_spring_boot.controller.TraineeController;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainee.TraineeProfileResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainee.TraineeUpdateResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.trainer.TrainerSummary;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TraineeTrainingResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.exception.GlobalExceptionHandler;
import pl.wojciechandrzejczak.gym_crm_spring_boot.facade.GymFacade;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.Trainee;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.User;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

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
                .standaloneSetup(new TraineeController(gymFacade))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void shouldRegisterTraineeAndReturnOnlyCredentials() throws Exception {
        LocalDate dateOfBirth = LocalDate.of(1995, 5, 10);

        User user = new User(
                "Jan",
                "Testowy",
                "Jan.Testowy",
                "test-password",
                true
        );

        Trainee trainee = new Trainee(user, dateOfBirth, "Warszawa");

        when(gymFacade.createTrainee(
                "Jan", "Testowy", dateOfBirth, "Warszawa"
        )).thenReturn(trainee);

        var response = mockMvc.perform(
                post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jan",
                                  "lastName": "Testowy",
                                  "dateOfBirth": "1995-05-10",
                                  "address": "Warszawa"
                                }
                                """)
        ).andReturn().getResponse();

        assertEquals(201, response.getStatus());
        assertNotNull(response.getContentType());
        assertTrue(MediaType.APPLICATION_JSON.isCompatibleWith(
                MediaType.parseMediaType(response.getContentType())
        ));

        var body = jsonMapper.readTree(response.getContentAsString());

        assertEquals(2, body.size());
        assertEquals("Jan.Testowy", body.get("username").asString());
        assertEquals("test-password", body.get("password").asString());

        verify(gymFacade).createTrainee(
                "Jan", "Testowy", dateOfBirth, "Warszawa"
        );
        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    void shouldAcceptMissingOptionalFields() throws Exception {
        User user = new User(
                "Jan",
                "Testowy",
                "Jan.Testowy",
                "test-password",
                true
        );

        when(gymFacade.createTrainee(
                "Jan", "Testowy", null, null
        )).thenReturn(new Trainee(user, null, null));

        var response = mockMvc.perform(
                post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jan",
                                  "lastName": "Testowy"
                                }
                                """)
        ).andReturn().getResponse();

        assertEquals(201, response.getStatus());

        verify(gymFacade).createTrainee(
                "Jan", "Testowy", null, null
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            """
            {
              "firstName": "",
              "lastName": "Testowy"
            }
            """,
            """
            {
              "firstName": "   ",
              "lastName": "Testowy"
            }
            """,
            """
            {
              "lastName": "Testowy"
            }
            """,
            """
            {
              "firstName": "Jan",
              "lastName": ""
            }
            """,
            """
            {
              "firstName": "Jan"
            }
            """
    })
    void shouldRejectInvalidNamesWithoutCallingFacade(String json)
            throws Exception {
        var response = mockMvc.perform(
                post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());
        assertNotNull(response.getContentType());
        assertTrue(MediaType.APPLICATION_PROBLEM_JSON.isCompatibleWith(
                MediaType.parseMediaType(response.getContentType())
        ));

        var body = jsonMapper.readTree(response.getContentAsString());

        assertEquals(400, body.get("status").asInt());
        assertEquals(
                "Request validation failed",
                body.get("detail").asString()
        );
        assertTrue(body.has("errors"));
        assertFalse(body.get("errors").isEmpty());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void shouldRejectInvalidDateWithoutCallingFacade() throws Exception {
        var response = mockMvc.perform(
                post("/api/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Jan",
                                  "lastName": "Testowy",
                                  "dateOfBirth": "not-a-date"
                                }
                                """)
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        var body = jsonMapper.readTree(response.getContentAsString());
        assertEquals(400, body.get("status").asInt());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void shouldReturnUnassignedTrainers() throws Exception {
        String username = "Jan.Testowy";
        String password = "test-password";

        List<TrainerSummary> trainers = List.of(
                new TrainerSummary(
                        "Adam.Trener",
                        "Adam",
                        "Trener",
                        TrainingTypeName.FITNESS
                )
        );

        when(gymFacade.getUnassignedTrainers(
                username, password
        )).thenReturn(trainers);

        var response = mockMvc.perform(
                get("/api/trainees/{username}/unassigned-trainers", username)
                        .header("Password", password)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        var body = jsonMapper.readTree(response.getContentAsString());

        assertTrue(body.isArray());
        assertEquals(1, body.size());

        var trainer = body.get(0);

        assertEquals("Adam.Trener", trainer.get("username").asString());
        assertEquals("Adam", trainer.get("firstName").asString());
        assertEquals("Trener", trainer.get("lastName").asString());
        assertEquals("FITNESS", trainer.get("specialization").asString());

        verify(gymFacade).getUnassignedTrainers(
                username, password
        );
        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    void shouldReturnTraineeProfile() throws Exception {
        String username = "Jan.Testowy";
        String password = "test-password";
        LocalDate dateOfBirth = LocalDate.of(1995, 5, 10);

        TrainerSummary trainer = new TrainerSummary(
                "Adam.Trener",
                "Adam",
                "Trener",
                TrainingTypeName.FITNESS
        );

        TraineeProfileResponse profile = new TraineeProfileResponse(
                "Jan",
                "Testowy",
                dateOfBirth,
                "Warszawa",
                true,
                List.of(trainer)
        );

        when(gymFacade.getTraineeProfile(username, password))
                .thenReturn(profile);

        var response = mockMvc.perform(
                get("/api/trainees/{username}", username)
                        .header("Password", password)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        var body = jsonMapper.readTree(response.getContentAsString());

        assertEquals("Jan", body.get("firstName").asString());
        assertEquals("Testowy", body.get("lastName").asString());
        assertEquals("1995-05-10", body.get("dateOfBirth").asString());
        assertEquals("Warszawa", body.get("address").asString());
        assertTrue(body.get("isActive").asBoolean());

        assertTrue(body.get("trainers").isArray());
        assertEquals(1, body.get("trainers").size());
        assertEquals(
                "Adam.Trener",
                body.get("trainers").get(0).get("username").asString()
        );

        verify(gymFacade).getTraineeProfile(username, password);
        verifyNoMoreInteractions(gymFacade);
    }
    @Test
    void shouldUpdateTraineeProfile() throws Exception {
        String username = "Jan.Testowy";
        String password = "test-password";
        LocalDate dateOfBirth = LocalDate.of(1996, 6, 15);

        TraineeUpdateResponse updatedProfile = new TraineeUpdateResponse(
                username,
                "Jan",
                "Nowak",
                dateOfBirth,
                "Poznan",
                true,
                List.of()
        );

        when(gymFacade.updateTraineeProfile(
                username,
                password,
                "Jan",
                "Nowak",
                dateOfBirth,
                "Poznan",
                true
        )).thenReturn(updatedProfile);

        var response = mockMvc.perform(
                put("/api/trainees/{username}", username)
                        .header("Password", password)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "firstName": "Jan",
                              "lastName": "Nowak",
                              "dateOfBirth": "1996-06-15",
                              "address": "Poznan",
                              "isActive": true
                            }
                            """)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        var body = jsonMapper.readTree(response.getContentAsString());

        assertEquals(username, body.get("username").asString());
        assertEquals("Jan", body.get("firstName").asString());
        assertEquals("Nowak", body.get("lastName").asString());
        assertEquals("1996-06-15", body.get("dateOfBirth").asString());
        assertEquals("Poznan", body.get("address").asString());
        assertTrue(body.get("isActive").asBoolean());

        verify(gymFacade).updateTraineeProfile(
                username,
                password,
                "Jan",
                "Nowak",
                dateOfBirth,
                "Poznan",
                true
        );
        verifyNoMoreInteractions(gymFacade);
    }
    @Test
    void shouldRejectTraineeUpdateWithoutRequiredFields() throws Exception {
        var response = mockMvc.perform(
                put("/api/trainees/Jan.Testowy")
                        .header("Password", "test-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "firstName": "",
                              "lastName": "",
                              "isActive": null
                            }
                            """)
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void shouldDeleteTrainee() throws Exception {
        String username = "Jan.Testowy";
        String password = "test-password";

        doNothing().when(gymFacade)
                .deleteTrainee(username, password);

        var response = mockMvc.perform(
                delete("/api/trainees/{username}", username)
                        .header("Password", password)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().isEmpty());

        verify(gymFacade).deleteTrainee(username, password);
        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    void shouldActivateTrainee() throws Exception {
        String username = "Jan.Testowy";
        String password = "test-password";

        var response = mockMvc.perform(
                patch("/api/trainees/{username}", username)
                        .header("Password", password)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "isActive": true
                            }
                            """)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        verify(gymFacade).activateTrainee(username, password);
        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    void shouldDeactivateTrainee() throws Exception {
        String username = "Jan.Testowy";
        String password = "test-password";

        var response = mockMvc.perform(
                patch("/api/trainees/{username}", username)
                        .header("Password", password)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "isActive": false
                            }
                            """)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        verify(gymFacade).deactivateTrainee(username, password);
        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    void shouldRejectActivityChangeWithoutIsActive() throws Exception {
        var response = mockMvc.perform(
                patch("/api/trainees/Jan.Testowy")
                        .header("Password", "test-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void shouldReturnEmptyListWhenNoUnassignedTrainers() throws Exception {
        String username = "Jan.Testowy";
        String password = "test-password";

        when(gymFacade.getUnassignedTrainers(username, password))
                .thenReturn(List.of());

        var response = mockMvc.perform(
                get("/api/trainees/{username}/unassigned-trainers", username)
                        .header("Password", password)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        var body = jsonMapper.readTree(response.getContentAsString());

        assertTrue(body.isArray());
        assertTrue(body.isEmpty());

        verify(gymFacade).getUnassignedTrainers(username, password);
        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    void shouldUpdateTraineeTrainers() throws Exception {
        String username = "Jan.Testowy";
        String password = "test-password";

        Set<String> trainerUsernames =
                Set.of("Adam.Trener", "Anna.Trener");

        List<TrainerSummary> trainers = List.of(
                new TrainerSummary(
                        "Adam.Trener",
                        "Adam",
                        "Trener",
                        TrainingTypeName.FITNESS
                ),
                new TrainerSummary(
                        "Anna.Trener",
                        "Anna",
                        "Trener",
                        TrainingTypeName.FITNESS
                )
        );

        when(gymFacade.updateTraineeTrainersList(
                username,
                password,
                trainerUsernames
        )).thenReturn(trainers);

        var response = mockMvc.perform(
                put("/api/trainees/{username}/trainers", username)
                        .header("Password", password)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "trainerUsernames": [
                                "Adam.Trener",
                                "Anna.Trener"
                              ]
                            }
                            """)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        var body = jsonMapper.readTree(response.getContentAsString());

        assertTrue(body.isArray());
        assertEquals(2, body.size());

        verify(gymFacade).updateTraineeTrainersList(
                username,
                password,
                trainerUsernames
        );
        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    void shouldAcceptEmptyTrainerList() throws Exception {
        String username = "Jan.Testowy";
        String password = "test-password";

        when(gymFacade.updateTraineeTrainersList(
                username,
                password,
                Set.of()
        )).thenReturn(List.of());

        var response = mockMvc.perform(
                put("/api/trainees/{username}/trainers", username)
                        .header("Password", password)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "trainerUsernames": []
                            }
                            """)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        verify(gymFacade).updateTraineeTrainersList(
                username,
                password,
                Set.of()
        );
    }

    @Test
    void shouldRejectMissingTrainerList() throws Exception {
        var response = mockMvc.perform(
                put("/api/trainees/Jan.Testowy/trainers")
                        .header("Password", "test-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void shouldReturnTraineeTrainingsWithFilters() throws Exception {
        String username = "Jan.Testowy";
        String authUsername = "Adam.Trener";
        String authPassword = "test-password";

        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 12, 31);

        List<TraineeTrainingResponse> trainings = List.of(
                new TraineeTrainingResponse(
                        "Morning training",
                        LocalDate.of(2026, 5, 10),
                        TrainingTypeName.FITNESS,
                        60,
                        "Adam Trener"
                )
        );

        when(gymFacade.getTraineeTrainingList(
                authUsername,
                authPassword,
                username,
                fromDate,
                toDate,
                "Adam Trener",
                TrainingTypeName.FITNESS
        )).thenReturn(trainings);

        var response = mockMvc.perform(
                get("/api/trainees/{username}/trainings", username)
                        .header("Username", authUsername)
                        .header("Password", authPassword)
                        .param("fromDate", "2026-01-01")
                        .param("toDate", "2026-12-31")
                        .param("trainerName", "Adam Trener")
                        .param("trainingType", "FITNESS")
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        var body = jsonMapper.readTree(response.getContentAsString());

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
                "Adam Trener",
                training.get("trainerName").asString()
        );

        verify(gymFacade).getTraineeTrainingList(
                authUsername,
                authPassword,
                username,
                fromDate,
                toDate,
                "Adam Trener",
                TrainingTypeName.FITNESS
        );

        verifyNoMoreInteractions(gymFacade);
    }
    @Test
    void shouldReturnTraineeTrainingsWithoutOptionalFilters()
            throws Exception {

        String username = "Jan.Testowy";
        String authUsername = "Jan.Testowy";
        String authPassword = "test-password";

        when(gymFacade.getTraineeTrainingList(
                authUsername,
                authPassword,
                username,
                null,
                null,
                null,
                null
        )).thenReturn(List.of());

        var response = mockMvc.perform(
                get("/api/trainees/{username}/trainings", username)
                        .header("Username", authUsername)
                        .header("Password", authPassword)
        ).andReturn().getResponse();

        assertEquals(200, response.getStatus());

        var body = jsonMapper.readTree(response.getContentAsString());

        assertTrue(body.isArray());
        assertTrue(body.isEmpty());

        verify(gymFacade).getTraineeTrainingList(
                authUsername,
                authPassword,
                username,
                null,
                null,
                null,
                null
        );

        verifyNoMoreInteractions(gymFacade);
    }
}