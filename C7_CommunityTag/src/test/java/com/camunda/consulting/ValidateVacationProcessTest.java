package com.camunda.consulting;

import com.camunda.consulting.delegates.SendMailDelegate;
import com.camunda.consulting.delegates.VacationCheckerDelegate;
import com.camunda.consulting.pojo.VacationRequest;
import com.camunda.consulting.services.SendMailService;
import com.camunda.consulting.services.ValidateVacationService;
import com.camunda.consulting.utils.VacationProcessConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit5.ProcessEngineCoverageExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@Deployment(resources = { "UrlaubsAntrag.bpmn" })
@ExtendWith(ProcessEngineCoverageExtension.class)
public class ValidateVacationProcessTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    SendMailService sendMailService;

    @Mock
    ValidateVacationService validateVacationService;

    @InjectMocks
    VacationCheckerDelegate vacationCheckerDelegate;

    @InjectMocks
    SendMailDelegate sendMailDelegate;

    @BeforeEach
    void setup() throws ParseException, JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        Mocks.register("vacationCheckerDelegate", vacationCheckerDelegate);
        Mocks.register("sendMailDelegate", sendMailDelegate);

        // When
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        Date startDate = formatter.parse("15.11.2022");
        Date endDate = formatter.parse("17.11.2022");
        VacationRequest vacationRequest = new VacationRequest("Norman", "Luering", "norman.luering@camunda.com", startDate, endDate);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(vacationRequest);
    }

    @Test
    public void testHappyPath() throws IOException {
        // when
        when(validateVacationService.validate(any(), any())).thenReturn(true);
        when(sendMailService.send(any(), anyString(), any())).thenReturn(202);

        // Start new Instance
        ProcessInstance processInstance = runtimeService().startProcessInstanceByMessage(VacationProcessConstants.VALIDATE_VACATION_MSG_START_EVENT, withVariables("request", "mockRequest"));
        assertThat(processInstance).isStarted();

        // Verify Validate Vacation Service Task
        assertThat(processInstance).isWaitingAt(VacationProcessConstants.VALIDATE_VACATION_SERVICE_TASK);
        execute(job());
        assertThat(processInstance).variables().containsEntry("approved", true);

        // Verify Book Vacation External Task
        assertThat(processInstance).isWaitingAt(VacationProcessConstants.BOOK_VACATION_SERVICE_TASK)
                .externalTask().hasTopicName(VacationProcessConstants.BOOK_VACATION_TOPIC_NAME);
        complete(externalTask());

        // Verify Send Mail Service Task
        assertThat(processInstance).isWaitingAt(VacationProcessConstants.SEND_APPROVAL_SERVICE_TASK);
        execute(job());
        assertThat(processInstance).variables().containsEntry("responseCode", 202);

        // Verify Process Path
        assertThat(processInstance).isEnded().hasPassed(VacationProcessConstants.PROCESS_APPROVAL_END)
                .hasPassed(VacationProcessConstants.SEND_APPROVAL_SERVICE_TASK)
                .hasPassed(VacationProcessConstants.BOOK_VACATION_SERVICE_TASK)
                .hasPassed(VacationProcessConstants.VALIDATE_VACATION_SERVICE_TASK);
    }

    @Test
    public void testRejectionPath() throws IOException {
        // when
        when(sendMailService.send(any(), anyString(), any())).thenReturn(202);

        // Start new Instance
        ProcessInstance processInstance = runtimeService().createProcessInstanceByKey(VacationProcessConstants.PROCESS_DEFINITION_KEY)
                .startAfterActivity(VacationProcessConstants.VALIDATE_VACATION_SERVICE_TASK)
                .setVariables(withVariables("approved", false, "request", "mockRequest"))
                .execute();
        assertThat(processInstance).isStarted();

        // Verify Send Mail Service Task
        assertThat(processInstance).isWaitingAt(VacationProcessConstants.SEND_REJECTION_SERVICE_TASK);
        execute(job());
        assertThat(processInstance).variables().containsEntry("responseCode", 202);

        // Verify Process Path
        assertThat(processInstance).isEnded().hasPassed(VacationProcessConstants.PROCESS_REJECTION_END)
                .hasPassed(VacationProcessConstants.SEND_REJECTION_SERVICE_TASK);
    }
}
