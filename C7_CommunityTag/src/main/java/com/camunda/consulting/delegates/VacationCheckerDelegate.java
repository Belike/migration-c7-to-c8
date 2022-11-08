package com.camunda.consulting.delegates;

import com.camunda.consulting.pojo.VacationRequest;
import com.camunda.consulting.services.ValidateVacationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VacationCheckerDelegate implements JavaDelegate {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    ValidateVacationService validateVacationService;

    @Override
    public void execute(DelegateExecution execution) throws JsonProcessingException {
        // Data Parsing & Mapping
        String requestJson = (String) execution.getVariable("request");
        VacationRequest vacationRequest = objectMapper.readValue(requestJson, VacationRequest.class);

        //Service Invocation
        log.info("Vacation Check will be initiated with StartDate {} and EndDate {}", vacationRequest.getStartDate(), vacationRequest.getEndDate());
        boolean approved = validateVacationService.validate(vacationRequest.getStartDate(), vacationRequest.getEndDate());

        //Output to Process
        execution.setVariable("approved", approved);
    }
}
