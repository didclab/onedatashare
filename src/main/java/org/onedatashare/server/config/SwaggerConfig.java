/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


package org.onedatashare.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
                        .host("http://onedatashare.org")
                        .select()
                        .apis(RequestHandlerSelectors.basePackage("org.onedatashare.server.controller"))
                        .paths(PathSelectors.any())
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
                    "REST API endpoints for OneDataShare",
                    "1.0",
                    null,
                    new Contact("OneDataShare Team","http://localhost:8080/support","admin@onedatashare.org"),
                    "Apache-2.0",
                    "https://github.com/didclab/onedatashare/blob/master/LICENSE",
                    Collections.EMPTY_LIST);
    }

}
