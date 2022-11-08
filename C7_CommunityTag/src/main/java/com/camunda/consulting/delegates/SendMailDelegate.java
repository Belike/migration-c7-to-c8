package com.camunda.consulting.delegates;

import com.camunda.consulting.pojo.VacationRequest;
import com.camunda.consulting.services.SendMailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SendMailDelegate implements JavaDelegate {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SendMailService sendMailService;

    @Override
    public void execute(DelegateExecution execution) throws IOException {
        // Data Parsing & Mapping
        String requestJson = (String) execution.getVariable("request");
        VacationRequest vacationRequest = objectMapper.readValue(requestJson, VacationRequest.class);
        Boolean approved = (Boolean) execution.getVariable("approved");

        // Prepare Mail
        Content content;
        String subject = "Urlaubsantrag";
        Email to = new Email(vacationRequest.getEmail());

        // Determine Approval Mail or Rejection Mail
        if(approved){
            content = new Content("text/plain", "Ihr Urlaub wurde genehmigt, Sie Glückspilz!");
        }else {
            content = new Content("text/plain", "Leider wurde Ihr Urlaubsantrag abgelehnt. Wenden Sie sich doch an den Betriebsrat!");
        }

        // Business Invocation
        int responseCode = sendMailService.send(to, subject, content);

        // Response Codes >= 400 are Errors, expected ResponseCode is 202 ( Accepted -> Ready for processing )
        if(responseCode >= 400){
            throw new IOException("Sending E-Mail resulted in ResponseCode " + responseCode);
        }

        // Append ResponseCode for Analyses
        execution.setVariable("responseCode", responseCode);
    }
}
