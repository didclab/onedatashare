package org.onedatashare.server.service;

import org.onedatashare.server.model.credential.OAuthCredential;
import org.springframework.stereotype.Service;


@Service
public interface OauthService {
  String start();
  OAuthCredential finish(String token);
}
