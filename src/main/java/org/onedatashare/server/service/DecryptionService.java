package org.onedatashare.server.service;

import org.onedatashare.server.model.useraction.UserActionCredential;
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


/**
 * Service class responsible for decrypting the RSA encrypted data received by the controller.
 * Primarily developed for password decryption for SFTP endpoint operations.
 *
 * Base code for generating the key pair can be found on the OneDataShare S3 bucket.
 */
@Service
public class DecryptionService {

    private String odsPrivateKey = System.getenv("ods_rsa_private_key");

    private PrivateKey privateKey;

    /**
     * Method invoked after this service is initialized in the application context.
     * Responsible for generating the privateKey object from the Base64 encoded odsPrivateKey string.
     */
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

    /**
     * Method that updates the input UserActionCredential object containing the RSA encrypted password with
     * the decrypted version of the password.
     *
     * @param cred - UserActionCredential object received in the HTTP request
     * @return Mono of cred object containing the decrypted password
     */
    public Mono<UserActionCredential> getDecryptedCredential(UserActionCredential cred){
        String encryptedPwd = cred.getPassword();
        cred.setPassword(getDecryptedPassword(encryptedPwd));
        return Mono.just(cred);
    }

    /**
     * Method that decrypted the input encrypted string.
     *
     * @param encryptedPwd - Base64 encoded encrypted password string
     * @return String containing decrypted version of the input string
     */
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
