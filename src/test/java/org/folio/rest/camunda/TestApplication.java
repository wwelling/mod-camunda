package org.folio.rest.camunda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class TestApplication extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(TestApplication.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }

}
