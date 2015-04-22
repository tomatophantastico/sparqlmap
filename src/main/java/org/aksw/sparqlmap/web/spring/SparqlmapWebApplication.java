package org.aksw.sparqlmap.web.spring;

import java.util.Arrays;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class SparqlmapWebApplication {
  public static void main(String[] args) {
    ApplicationContext ctx = SpringApplication.run(WebAppConfig.class, args);

    System.out.println("Let's inspect the beans provided by Spring Boot:");

    String[] beanNames = ctx.getBeanDefinitionNames();
    Arrays.sort(beanNames);
    for (String beanName : beanNames) {
        System.out.println(beanName);
    }
}
}
