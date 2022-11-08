package com.camunda.consulting.externalTaskWorker;

import com.camunda.consulting.pojo.VacationRequest;
import com.camunda.consulting.services.VacationBookingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskHandler;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ExternalTaskSubscription("vacationBooking")
@Slf4j
public class VacationBookingWorker implements ExternalTaskHandler {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    VacationBookingService vacationBookingService;

    @Override
    // No Retry Behavior - please do not treat this as a good example for External Tasks!
    public void execute(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        try {
            // Data Parsing & Mapping
            String requestJson = (String) externalTask.getVariable("request");
            VacationRequest vacationRequest = objectMapper.readValue(requestJson, VacationRequest.class);

            // Business Service Invocation
            vacationBookingService.bookVacation(vacationRequest.getStartDate(), vacationRequest.getEndDate());

            //Response to Engine
            externalTaskService.complete(externalTask);
        } catch (JsonProcessingException e) {
            log.info("Unforseen Error results in Failure. Reason {}", e.getMessage());
            externalTaskService.handleFailure(externalTask, e.getClass().getCanonicalName(), e.getMessage(), 0, 0);
            throw new RuntimeException(e);
        }
    }
}
