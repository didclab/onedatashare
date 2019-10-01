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


@Profile("!local")
public class SwaggerController {

    @Profile("!dev")
    @RestController
    @Slf4j
    public static class NoDevConfig {

        @RequestMapping(value = "swagger-ui.html", method = RequestMethod.GET)
        public void getSwagger(HttpServletResponse httpResponse) throws IOException {
            httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
