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
import pl.wojciechandrzejczak.gym_crm_spring_boot.controller.TrainingTypeController;
import pl.wojciechandrzejczak.gym_crm_spring_boot.dto.training.TrainingTypeResponse;
import pl.wojciechandrzejczak.gym_crm_spring_boot.exception.GlobalExceptionHandler;
import pl.wojciechandrzejczak.gym_crm_spring_boot.facade.GymFacade;
import pl.wojciechandrzejczak.gym_crm_spring_boot.model.TrainingTypeName;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerTest {

    @Mock
    private GymFacade gymFacade;

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    private final JsonMapper jsonMapper =
            JsonMapper.builder().build();

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        new TrainingTypeController(gymFacade)
                )
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }


    @Test
    void shouldReturnTrainingTypes() throws Exception {
        String username = "Jan.Testowy";
        String password = "test-password";

        List<TrainingTypeResponse> trainingTypes =
                List.of(
                        new TrainingTypeResponse(
                                1L,
                                TrainingTypeName.FITNESS
                        ),
                        new TrainingTypeResponse(
                                2L,
                                TrainingTypeName.YOGA
                        )
                );

        when(gymFacade.getTrainingTypes(
                username,
                password
        )).thenReturn(trainingTypes);

        var response = mockMvc.perform(
                get("/api/training-types")
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

        assertTrue(body.isArray());
        assertEquals(2, body.size());

        assertEquals(
                1L,
                body.get(0).get("trainingTypeId").asLong()
        );

        assertEquals(
                "FITNESS",
                body.get(0).get("trainingType").asString()
        );

        assertEquals(
                2L,
                body.get(1).get("trainingTypeId").asLong()
        );

        assertEquals(
                "YOGA",
                body.get(1).get("trainingType").asString()
        );

        verify(gymFacade).getTrainingTypes(
                username,
                password
        );

        verifyNoMoreInteractions(gymFacade);
    }

    @Test
    void shouldReturnEmptyTrainingTypesList()
            throws Exception {

        String username = "Jan.Testowy";
        String password = "test-password";

        when(gymFacade.getTrainingTypes(
                username,
                password
        )).thenReturn(List.of());

        var response = mockMvc.perform(
                get("/api/training-types")
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

        var body = jsonMapper.readTree(
                response.getContentAsString()
        );

        assertTrue(body.isArray());
        assertTrue(body.isEmpty());

        verify(gymFacade).getTrainingTypes(
                username,
                password
        );

        verifyNoMoreInteractions(gymFacade);
    }


    @Test
    void shouldRejectRequestWithoutUsernameHeader()
            throws Exception {

        var response = mockMvc.perform(
                get("/api/training-types")
                        .header(
                                "Password",
                                "test-password"
                        )
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(gymFacade);
    }

    @Test
    void shouldRejectRequestWithoutPasswordHeader()
            throws Exception {

        var response = mockMvc.perform(
                get("/api/training-types")
                        .header(
                                "Username",
                                "Jan.Testowy"
                        )
        ).andReturn().getResponse();

        assertEquals(400, response.getStatus());

        verifyNoInteractions(gymFacade);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   "
    })
    void shouldRejectBlankUsername(String username)
            throws Exception {

        var response = mockMvc.perform(
                get("/api/training-types")
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

        verifyNoInteractions(gymFacade);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   "
    })
    void shouldRejectBlankPassword(String password)
            throws Exception {

        var response = mockMvc.perform(
                get("/api/training-types")
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

        verifyNoInteractions(gymFacade);
    }
}