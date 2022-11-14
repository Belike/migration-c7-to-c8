## C8 Part of the Project

This is the migrated process "UrlaubsAntrag.bpmn".
You need to provide an active cluster and append configuration to application.yaml.

Camunda SaaS Trial would be the easiest startup.

Project is tailored for JDK 17+.
If JDK 8+ is required, change pom.xml as described here: https://github.com/camunda-community-hub/spring-zeebe#writing-test-cases

## C8 Engine

Trial SaaS can be obtained here: https://accounts.cloud.camunda.io/signup?utm_source=docs.camunda.io&utm_medium=referral
For SM please take a look here: https://docs.camunda.io/docs/self-managed/platform-deployment/helm-kubernetes/overview/



## Start a new Process
Process Start can best be achieved by running zbctl with:
Example start parameters can be found in test/resources

zbctl publish message 'VacationStart_Message' --correlationKey="" --variables=./startParameterApproval.json
--address YOUR_ADDRESS --clientId YOUR_CLIENT_ID --clientSecret YOUR_CLIENT_SECRET


zbctl publish message 'VacationStart_Message'--correlationKey="test" --variables=./startParameterApproval.json --address '6224ece9-356f-41a3-904d-533e0bfd45aa.bru-2.zeebe.camunda.io:443' --clientId 'OpL7zK8kRcENrRbWa53rrgQLfUzdULPb' --clientSecret 'OpL7zK8kRcENrRbWa53rrgQLfUzdULPb' --authzUrl 'https://login.cloud.camunda.io/oauth/token'

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