package com.camunda.consulting.worker;

import com.camunda.consulting.pojo.VacationRequest;
import com.camunda.consulting.services.SendMailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class SendMailWorker {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SendMailService sendMailService;

    // Autocomplete = true default
    // Returning String, Map or Object will result in process variables
    @JobWorker(type = "sendMail")
    public Map<String, Object> execute(final ActivatedJob job) throws IOException {
        // Data Parsing & Mapping
        VacationRequest vacationRequest = objectMapper.convertValue(job.getVariablesAsMap().get("request"), VacationRequest.class);
        Boolean approved = (Boolean) job.getVariablesAsMap().get("approved");

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
        return Map.of("responseCode", responseCode);
    }
}
