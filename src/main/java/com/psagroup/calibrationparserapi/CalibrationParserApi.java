package com.psagroup.calibrationparserapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class CalibrationParserApi {

  public static void main(String[] args) {
    SpringApplication.run(CalibrationParserApi.class, args);
  }

  
  // Swagger Configuration

  @Bean
  public Docket attmApi() {
    return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select()
        .apis(RequestHandlerSelectors.basePackage("com.psagroup.calibrationparserapi"))
        .paths(PathSelectors.any())
        .build();
  }

  
  

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
        .title("A2L Parser API")
        .version("0.3.0").build();
  }
  
  
  
  
  
}
