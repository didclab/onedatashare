package org.onedatashare.server.service.oauth;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class DbxConfig {
    @Value("${dropbox.key}")
    private String key;
    @Value("${dropbox.secret}")
    private String secret;
    @Value("${dropbox.redirectUri}")
    private String redirectUri;
    @Value("${dropbox.identifier}")
    private String identifier;
}
