package org.onedatashare.server;

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

    private ApiInfo apiInfo() {
        return new ApiInfo("title",
                    "description",
                    "version",
                    "termsOfServiceUrl",
                    new Contact("name","url","email"),
                    "license",
                    "licenseUrl",
                    Collections.EMPTY_LIST);
    }
}
