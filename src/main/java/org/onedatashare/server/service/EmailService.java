package org.onedatashare.server.service;

import com.amazonaws.services.simpleemail.model.*;
import com.amazonaws.services.simpleemail.model.Message;
import org.springframework.stereotype.Service;

import javax.mail.*;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

/**
 * This service class is responsible to send all emails for OneDataShare.
 * Currently uses a AWS SimpleEmailService for sending out emails
 *
 * @version 1.0
 * @since 03-Feb-2020
 */
@Service
public class EmailService {

    private final String EMAIL_USERNAME = System.getenv("ODS_EMAIL_ADDRESS");
    private final String AWS_ACCESS_KEY = System.getenv("AWS_ACCESS_KEY");
    private final String AWS_SECRET_KEY = System.getenv("AWS_SECRET_KEY");

    /**
     * Method that creates and sends an email
     *
     * @param emailTo - email recipient
     * @param subject - email subject
     * @param emailText - content of the email
     * @throws MessagingException - throws an exception if there was an error in sending email. Must be caught in referenced classes.
     */

    public void sendEmail(String emailTo, String subject, String emailText){
        try {
            AmazonSimpleEmailService client = getAWSClient();
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(emailTo))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(emailText))
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(emailText)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(subject)))
                    .withSource(EMAIL_USERNAME);
            client.sendEmail(request);
            ODSLoggerService.logInfo("Sent email with subject \"" + subject + "\" to \""+ emailTo + "\" successfully.");
        }
        catch (Exception ex) {
            ODSLoggerService.logError("Failure in sending email with " + subject + " to " + emailTo, ex);
        }
    }

    /**
     *  This method returns the sending limit from the past 24hrs period.
     */
    public GetSendQuotaResult getSendQuota() {
        try {
            AmazonSimpleEmailService client = getAWSClient();
            GetSendQuotaRequest request = new GetSendQuotaRequest();
            GetSendQuotaResult response = client.getSendQuota(request);
            return response;
        } catch (Exception e) {
            ODSLoggerService.logError("Failure in getting email quota ", e);
        }
        return null;
    }

    /**
     * This method returns the AWS Email Service object
     * @return
     */
    private AmazonSimpleEmailService getAWSClient(){
        return AmazonSimpleEmailServiceClientBuilder.standard().withCredentials(new AWSCredentialsProvider() {
            @Override
            public AWSCredentials getCredentials() {
                return new AWSCredentials() {
                    @Override
                    public String getAWSSecretKey() {
                        return AWS_SECRET_KEY;
                    }

                    @Override
                    public String getAWSAccessKeyId() {
                        return AWS_ACCESS_KEY;
                    }
                };
            }

            @Override
            public void refresh() {

            }
        }).withRegion(Regions.US_EAST_1).build();

    }

    public boolean isValidEmail(String email){
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }
}
