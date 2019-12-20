package org.onedatashare.server.service;

import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * Service that performs server side validation functionality of Google ReCaptcha V2
 * by validating the verification value with the secret key using the Google siteverify API.
 *
 * @version 1.0
 * @since 05-08-2019
 */
@Service                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        
public class CaptchaService {

    private String GOOGLE_CAPTCHA_VERIFY_API_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final String secretKey = System.getenv("GOOGLE_CAPTCHA_SECRET");


    private final String REQUEST_METHOD = "POST";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Method that performs server side validation.
     *
     * @param verificationValue
     * @return a boolean value if the verification was successful with the passed verificatioValue from frontend
     */
    public Mono<Boolean> verifyValue(String verificationValue){
        String errorMsg = "";
        ODSLoggerService.logInfo("In CaptchaService.verifyValue: Received verificationValue of " + verificationValue);

        try{
            URL urlObj = new URL(GOOGLE_CAPTCHA_VERIFY_API_URL);
            HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();

            conn.setRequestMethod(REQUEST_METHOD);

            String postParams = "secret=" + secretKey + "&response=" + verificationValue;

            conn.setDoOutput(true);
            DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
            outputStream.writeBytes(postParams);
            outputStream.flush();
            outputStream.close();

            int respCode = conn.getResponseCode();

            if(respCode == HttpURLConnection.HTTP_OK) {
                StringBuffer resp = new StringBuffer();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String input = null;
                while ((input = br.readLine()) != null)
                    resp.append(input);

                GoogleCaptchaVerifyResponse respObj = objectMapper.readValue(resp.toString(), GoogleCaptchaVerifyResponse.class);
                return Mono.just(respObj.getSuccess());
            }
            else{
                ODSLoggerService.logError("There was an error verifying the captcha code - " + conn.getResponseMessage());
            }
        }
        catch(MalformedURLException mue){
            ODSLoggerService.logError("Exception occurred while creating URL object",mue);
        }
        catch(IOException ioe){
            ODSLoggerService.logError("Exception occurred while opening or reading from a connection with "
                                            + GOOGLE_CAPTCHA_VERIFY_API_URL, ioe);
        }
        catch (Exception ex){
            ODSLoggerService.logError("General error occurred while verify captcha", ex);
        }
        return Mono.error(new Exception(errorMsg));
    }
}

/**
 * Model for response from Google siteverify API.
 */
@Data
class GoogleCaptchaVerifyResponse{

    private Boolean success;
    private Date challenge_ts;
    private String hostname;
    @JsonProperty("error-codes")
    private String[] error_codes;

}
