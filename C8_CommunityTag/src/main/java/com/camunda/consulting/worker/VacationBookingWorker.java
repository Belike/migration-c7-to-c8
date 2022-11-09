package com.camunda.consulting.worker;

import com.camunda.consulting.pojo.VacationRequest;
import com.camunda.consulting.services.VacationBookingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VacationBookingWorker {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    VacationBookingService vacationBookingService;

    @JobWorker(type = "vacationBooking")
    public void execute(ActivatedJob job) {
        // Data Parsing & Mapping
        VacationRequest vacationRequest = objectMapper.convertValue(job.getVariablesAsMap().get("request"), VacationRequest.class);

        // Business Service Invocation
        vacationBookingService.bookVacation(vacationRequest.getStartDate(), vacationRequest.getEndDate());
    }
}
