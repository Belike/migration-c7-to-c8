package com.camunda.consulting;

import com.camunda.consulting.services.VacationBookingService;
import com.camunda.consulting.worker.SendMailWorker;
import com.camunda.consulting.worker.VacationBookingWorker;
import com.camunda.consulting.worker.VacationCheckerWorker;
import com.camunda.consulting.pojo.VacationRequest;
import com.camunda.consulting.services.SendMailService;
import com.camunda.consulting.services.ValidateVacationService;
import com.camunda.consulting.utils.VacationProcessConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.*;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.inspections.InspectionUtility;
import io.camunda.zeebe.process.test.inspections.model.InspectedProcessInstance;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.*;
import static org.mockito.Mockito.*;

// This is for JDK 17 +
// JDK 8+ with Docker is also possible with
// import io.camunda.zeebe.process.test.extension.testcontainer.ZeebeProcessTest;

@SpringBootTest
@ZeebeSpringTest
public class ValidateVacationProcessTest {


    @Autowired
    ZeebeClient zeebeClient;

    @Autowired
    ZeebeTestEngine zeebeTestEngine;
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SendMailService sendMailService;

    @Mock
    private ValidateVacationService validateVacationService;

    @Mock
    VacationBookingService vacationBookingService;

    @Autowired
    @InjectMocks
    VacationCheckerWorker vacationCheckerWorker;

    @Autowired
    @InjectMocks
    SendMailWorker sendMailWorker;

    @Autowired
    @InjectMocks
    VacationBookingWorker vacationBookingWorker;

    private ProcessInstanceEvent startProcess() {
        return this.zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(VacationProcessConstants.PROCESS_DEFINITION_KEY)
                .latestVersion()
                .send().join();
    }

    @BeforeEach
    void setup() throws InterruptedException, TimeoutException {
        MockitoAnnotations.openMocks(this);
        zeebeTestEngine.waitForIdleState(Duration.ofSeconds(10));

        zeebeClient.newDeployResourceCommand()
                .addResourceFromClasspath("UrlaubsAntrag.bpmn")
                .send().join();

    }

    @Test
    public void testHappyPath() throws IOException, ParseException {
        // when
        VacationRequest vacationRequest = createDummyVacationRequest("15.11.2022", "17.11.2022");
        when(validateVacationService.validate(any(), any())).thenReturn(true);
        when(sendMailService.send(any(), anyString(), any())).thenReturn(202);
        when(objectMapper.convertValue(any(), any(Class.class))).thenReturn(vacationRequest);


        // Start new Instance
        zeebeClient.newPublishMessageCommand().messageName(VacationProcessConstants.VALIDATE_VACATION_MSG_START_EVENT).correlationKey("")
                .variables(Map.of("request", "mockRequest")).send().join();


        InspectedProcessInstance processInstance = InspectionUtility.findProcessInstances().findLastProcessInstance().get();

        // Verify Validate Vacation Service Task
        waitForProcessInstanceCompleted(processInstance);
        assertThat(processInstance).isCompleted()
                .hasPassedElement(VacationProcessConstants.PROCESS_APPROVAL_END)
                .hasPassedElement(VacationProcessConstants.SEND_APPROVAL_SERVICE_TASK)
                .hasPassedElement(VacationProcessConstants.BOOK_VACATION_SERVICE_TASK)
                .hasPassedElement(VacationProcessConstants.VALIDATE_VACATION_SERVICE_TASK)
                .hasVariableWithValue("approved", true)
                .hasVariable("request")
                .hasVariableWithValue("responseCode", 202);

        verify(sendMailService, times(1)).send(any(), anyString(), any());
        verify(validateVacationService, times(1)).validate(any(), any());
    }

    @Test
    public void testRejectionPath() throws IOException, ParseException {
        //when
        VacationRequest vacationRequest = createDummyVacationRequest("15.11.2022", "01.01.2023");
        when(validateVacationService.validate(any(), any())).thenReturn(false);
        when(sendMailService.send(any(), anyString(), any())).thenReturn(202);
        when(objectMapper.convertValue(any(), any(Class.class))).thenReturn(vacationRequest);

        zeebeClient.newPublishMessageCommand().messageName(VacationProcessConstants.VALIDATE_VACATION_MSG_START_EVENT).correlationKey("")
                .variables(Map.of("request", "mockRequest")).send().join();

        InspectedProcessInstance processInstance = InspectionUtility.findProcessInstances().findLastProcessInstance().get();
        BpmnAssert.assertThat(processInstance).isStarted();

        waitForProcessInstanceCompleted(processInstance);

        assertThat(processInstance).isCompleted()
                .hasPassedElement(VacationProcessConstants.PROCESS_REJECTION_END)
                .hasPassedElement(VacationProcessConstants.SEND_REJECTION_SERVICE_TASK)
                .hasPassedElement(VacationProcessConstants.VALIDATE_VACATION_SERVICE_TASK)
                .hasVariableWithValue("approved", false)
                .hasVariable("request")
                .hasVariableWithValue("responseCode", 202);

        verify(sendMailService, times(1)).send(any(), anyString(), any());
        verify(validateVacationService, times(1)).validate(any(), any());
    }

    private VacationRequest createDummyVacationRequest(String startDate, String endDate) throws ParseException {

        // When
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        VacationRequest vacationRequest = new VacationRequest("Norman", "Luering",
                "norman.luering@camunda.com", formatter.parse(startDate), formatter.parse(endDate));

        return vacationRequest;
    }
}
