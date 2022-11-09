package com.camunda.consulting;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@Slf4j
@EnableZeebeClient
@Deployment(resources = "classpath:UrlaubsAntrag.bpmn")
public class CamundaApplication {

  @Autowired
  ZeebeClient zeebeClient;

  public static void main(String... args) {
    ConfigurableApplicationContext context = SpringApplication.run(CamundaApplication.class, args);
  }

}