package com.camunda.consulting.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Calendar;
import java.util.Date;

@Component
@Slf4j
public class ValidateVacationService {

    public boolean validate(Date startDate, Date endDate){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);

        if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
           calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            log.info("Vacation can not start on Saturday/Sundays");
            return false;
        }else if(Duration.between(startDate.toInstant(), endDate.toInstant()).toDays() > 7){
            log.info("Company Info - Vacation Maximum is 7 Days!");
            return false;
        }else {
            return true;
        }
    }
}
