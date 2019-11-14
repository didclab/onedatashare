package org.onedatashare.server.service;

import org.jetbrains.annotations.Contract;
import org.onedatashare.server.model.useraction.UserActionCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;

@Service
public class DecryptionService {

    @Value("${ods.rsa.private.key}")
    private String odsPrivateKey;

    private PrivateKey privateKey;

    @PostConstruct
    public void createPrivateKey(){
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(odsPrivateKey.getBytes()));
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            ODSLoggerService.logError("Error occurred while instantiating key factory");
            e.printStackTrace();
        }
        try {
            privateKey = keyFactory.generatePrivate(keySpec);
        } catch (InvalidKeySpecException e) {
            ODSLoggerService.logError("Error occurred while instantiating private key");
            e.printStackTrace();
        }
    }

    public Mono<UserActionCredential> getDecryptedCredential(UserActionCredential cred){
        String encryptedPwd = cred.getPassword();
        cred.setPassword(getDecryptedPassword(encryptedPwd));
        return Mono.just(cred);
    }

    public String getDecryptedPassword(String encryptedPwd){
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] encPwdBytes = Base64.getDecoder().decode(encryptedPwd.getBytes());
            return new String(cipher.doFinal( encPwdBytes ));
        }
        catch(Exception e){
            ODSLoggerService.logError("Error occurred while decrypting password");
            e.printStackTrace();
        }

        return null;
    }
}
