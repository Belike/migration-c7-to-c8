## C7 Part of the Project

This is the base project for the migration example.
It utilizes the Spring Boot Camunda Embedded Engine and by default exposes Swagger Ui on localhost:8080/swaggerui

Process is started by a Message Start Event that can best be sent via REST API.

Please find example payloads below:


## Message Start: Approval

{
"messageName": "VacationStart_Message",
"businessKey": "001",
"processVariables": {
"request": {
"value": "{ \"firstName\" : \"Norman\", \"lastName\" : \"Luering\", \"email\" : \"norman.luering@camunda.com\", \"startDate\" : \"15.11.2022\", \"endDate\" : \"21.11.2022\"}",
"type": "String"
}
}
}
}

## Message Start: Rejection

{
"messageName": "VacationStart_Message",
"businessKey": "001",
"processVariables": {
"request": {
"value": "{ \"firstName\" : \"Norman\", \"lastName\" : \"Luering\", \"email\" : \"norman.luering@camunda.com\", \"startDate\" : \"15.11.2022\", \"endDate\" : \"01.01.2023\"}",
"type": "String"
}
}
}
}

## Tests

Run mvn tests for process flow and service mockup tests

## Known Limitations

It utilizes h2 in-memory database. If you need a fresh project, please also remove *.db files.