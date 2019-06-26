package org.onedatashare.server.service.oauth;

import com.dropbox.core.*;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;
import org.onedatashare.module.globusapi.GlobusClient;
import org.onedatashare.server.model.core.Credential;
import org.onedatashare.server.model.credential.OAuthCredential;
import org.onedatashare.server.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.result.view.RedirectView;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class GridftpAuthService {
    private GlobusClient globusclient = new GlobusClient();

    public synchronized String start() {
        try {
            // Authorize the DbxWebAuth auth as well as redirect the user to the finishURI, done this way to appease OAuth 2.0
            return globusclient.generateAuthURL().block();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized Mono<OAuthCredential> finish(String token) {
        try {
            return globusclient.getAccessToken(token).map(
                    acctoken -> {
                        OAuthCredential oa = new OAuthCredential(acctoken.getTransferAccessToken());
                        oa.expiredTime = acctoken.getExpiredTime();
                        oa.name = "GridFTP Client";
                        return oa;
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
