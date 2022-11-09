package com.camunda.consulting.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class VacationBookingService {

    public void bookVacation(Date startDate, Date endDate) {
        log.info("I am booking your vacation internally for Start Date {} and End Date {}", startDate, endDate);
    }
}
