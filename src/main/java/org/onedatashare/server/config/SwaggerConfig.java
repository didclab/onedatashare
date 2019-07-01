package org.onedatashare.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

import java.util.Collections;
import java.util.Optional;

/**
 * Class to configure SwaggerUI
 *
 * References:
 *      https://youtu.be/Bgn4V8wpVDY
 *      http://springfox.github.io/springfox/docs/snapshot/#getting-started
 */
@Configuration
@EnableSwagger2WebFlux
public class SwaggerConfig {

    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                        .select()
                        .apis(RequestHandlerSelectors.basePackage("org.onedatashare.server.controller"))
                        .paths(PathSelectors.ant("/**"))
                        .build()
                        .genericModelSubstitutes(Mono.class, Flux.class, Optional.class)
                        .apiInfo(apiInfo());
    }

    /**
     * Generates the API info that will be displayed in the header section of Swagger-UI
     * @return ApiInfo - springfox model containing ODS information
     */
    private ApiInfo apiInfo() {
        return new ApiInfo("OneDataShare API Docs",
                    "This page lists all the API endpoints for OneDataShare application",
                    "1.0",
                    null,
                    new Contact("OneDataShare Team","http://localhost:8080/support","admin@onedatashare.org"),
                    null,
                    null,
                    Collections.EMPTY_LIST);
    }
}
