## C8 Part of the Project

This is the migrated process "UrlaubsAntrag.bpmn".
You need to provide an active cluster and append configuration to application.yaml.

Camunda SaaS Trial would be the easiest startup.

Project is tailored for JDK 17+.
If JDK 8+ is required, change pom.xml as described here: https://github.com/camunda-community-hub/spring-zeebe#writing-test-cases


## Start a new Process
Process Start can best be achieved by running zbctl with:
Example start parameters can be found in test/resources

zbctl publish message 'VacationStart_Message' --correlationKey="" --variables=./startParameterApproval.json
--address YOUR_ADDRESS --clientId YOUR_CLIENT_ID --clientSecret YOUR_CLIENT_SECRET

## Tests

Run mvn tests for process flow and service mockup tests

## Known Limitations

It can happen that the Zeebe Test Engine picks up Environment Variables:
- ZEEBE_ADDRESS
- ZEEBE_CLIENT_ID 
- ZEEBE_CLIENT_SECRET
- ZEEBE_AUTHORIZATION_SERVER_URL

Please check if you get 401 Unauthorized errors and remove environment variables for the meantime. 
This bug has already been mitigated in the underlying project and will be shipped to the Spring wrapper very soon!