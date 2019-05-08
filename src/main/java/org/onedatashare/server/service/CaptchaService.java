package org.onedatashare.server.service;

import lombok.Data;
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
 *
 */
@Service
public class CaptchaService {

    private String GOOGLE_CAPTCHA_VERIFY_API_URL = "https://www.google.com/recaptcha/api/siteverify";

    private final String secretKey = System.getenv("GOOGLE_CAPTCHA_SECRET");

    private final String REQUEST_METHOD = "POST";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Mono<Boolean> verifyValue(String verificationValue){
        String errorMsg = "";

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
            System.out.println(respCode);

            StringBuffer resp = new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String input = null;
            while((input = br.readLine()) != null)
                resp.append(input);

            GoogleCaptchaVerifyResponse respObj = objectMapper.readValue(resp.toString(), GoogleCaptchaVerifyResponse.class);
            return Mono.just(respObj.getSuccess());
        }
        catch(MalformedURLException mue){
            errorMsg = "Exception occurred while creating URL object";
            mue.printStackTrace();
        }
        catch(IOException ioe){
            errorMsg = "Exception occurred while opening or reading from a connection with " + GOOGLE_CAPTCHA_VERIFY_API_URL;
            ioe.printStackTrace();
        }
        catch (Exception e){
            errorMsg = "General error occurred while verify captcha";
            e.printStackTrace();
        }

        System.out.println(errorMsg);
        return Mono.error(new Exception(errorMsg));
    }
}

@Data
class GoogleCaptchaVerifyResponse{

    private Boolean success;
    private Date challenge_ts;
    private String hostname;
    private String[] error_codes;

}
