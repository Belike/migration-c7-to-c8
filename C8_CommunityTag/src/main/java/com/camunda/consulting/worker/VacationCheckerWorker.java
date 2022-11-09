package com.camunda.consulting.worker;

import com.camunda.consulting.pojo.VacationRequest;
import com.camunda.consulting.services.ValidateVacationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class VacationCheckerWorker {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    ValidateVacationService validateVacationService;

    @JobWorker(type = "vacationCheck")
    public Map<String, Object> execute(final ActivatedJob job) throws JsonProcessingException {
        // Data Parsing & Mapping
        VacationRequest vacationRequest = objectMapper.convertValue(job.getVariablesAsMap().get("request"), VacationRequest.class);

        //Service Invocation
        log.info("Vacation Check will be initiated with StartDate {} and EndDate {}", vacationRequest.getStartDate(), vacationRequest.getEndDate());
        boolean approved = validateVacationService.validate(vacationRequest.getStartDate(), vacationRequest.getEndDate());

        //Output to Process
        return Map.of("approved", approved);
    }
}
