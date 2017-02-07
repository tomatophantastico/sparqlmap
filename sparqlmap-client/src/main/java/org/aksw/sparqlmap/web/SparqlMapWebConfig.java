package org.aksw.sparqlmap.web;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.ErrorMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
//ErrorMvcAutoConfiguration.class,
@EnableAutoConfiguration(exclude={CassandraAutoConfiguration.class, DataSourceAutoConfiguration.class})
@ComponentScan("org.aksw.sparqlmap.web.dto")
public class SparqlMapWebConfig {
  
  

  
  @Bean
  public Docket sparqlMapApi(){
    return new Docket(DocumentationType.SWAGGER_2)
        .select()
          .apis(RequestHandlerSelectors.basePackage("org.aksw.sparqlmap.web"))
          .paths(PathSelectors.any())
          .build()
        ;
  }

}
