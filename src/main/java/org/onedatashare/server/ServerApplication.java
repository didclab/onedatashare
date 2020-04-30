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


package org.onedatashare.server;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@OpenAPIDefinition(
        info = @Info(
                title = "OneDataShare REST API",
                version = "1.0",
                description = "OpenAPI REST API documentation",
                license = @License(name = "Apache-2.0", url = "https://github.com/didclab/onedatashare/blob/master/LICENSE"),
                contact = @Contact(url = "http://onedatashare.org", name = "OneDataShare Team", email = "admin@onedatashare.org")
        ),
        servers = {
                @Server(
                        description = "ODS backend",
                        url = "http://onedatashare.org"
                ) 
        }
)

@SpringBootApplication
public class ServerApplication {

  public static void main(String[] args) {
    BasicConfigurator.configure();
    SpringApplication.run(ServerApplication.class, args);
  }
}
