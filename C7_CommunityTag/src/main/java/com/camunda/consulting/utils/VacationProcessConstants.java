package com.camunda.consulting.utils;

public class VacationProcessConstants {

  // Events
  public static final String PROCESS_DEFINITION_KEY = "ValidateVacationProcess";
  public static final String PROCESS_APPROVAL_END = "VacationProcess_ApprovalEndEvent";
  public static final String PROCESS_REJECTION_END = "VacationProcess_RejectionEndEvent";

  // Tasks
  public static final String VALIDATE_VACATION_SERVICE_TASK = "ValidateVacation_ServiceTask";
  public static final String BOOK_VACATION_SERVICE_TASK = "BookVacation_ExternalServiceTask";
  public static final String SEND_APPROVAL_SERVICE_TASK = "SendApproval_ServiceTask";
  public static final String SEND_REJECTION_SERVICE_TASK = "SendRejection_ServiceTask";

  // Topic Names
  public static final String BOOK_VACATION_TOPIC_NAME = "vacationBooking";


  // Message Names
  public static final String VALIDATE_VACATION_MSG_START_EVENT = "VacationStart_Message";
}
