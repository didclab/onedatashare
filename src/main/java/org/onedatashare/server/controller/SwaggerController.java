package org.onedatashare.server.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This controller class is only responsible for showing error page if swagger-ui.html is not loaded because of application is running in profiles other than local.
 * Currently Swagger-ui.html does not have any authentication based on user or ip but it will load only for local profile.
 * To know what profile is please see @Profile in SpringBoot.
 * @version 1.0
 * @since 10-01-2019
 */

@Profile("!local")// if profile is not local.
@RestController
@Slf4j
public class SwaggerController {

    @RequestMapping(value = "swagger-ui.html", method = RequestMethod.GET)
    public void getSwagger(HttpServletResponse httpResponse) throws IOException {
        httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
