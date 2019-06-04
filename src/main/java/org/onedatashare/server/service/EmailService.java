package org.onedatashare.server.service;

import org.springframework.stereotype.Service;

import javax.mail.*;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;

/**
 * This service class is responsible to send all emails for OneDataShare.
 * Currently uses a Google account to send emails but will be replaced by an ODS email server in near future.
 *
 * @version 1.0
 * @since 05-05-2019
 */
@Service
public class EmailService {

    final String EMAIL_USERNAME = System.getenv("ODS_EMAIL_ADDRESS");
    final String AWS_ACCESS_KEY = System.getenv("AWS_ACCESS_KEY");
    final String AWS_SECRET_KEY = System.getenv("AWS_SECRET_KEY");

    /**
     * Method that creates and sends an email
     *
     * @param emailTo - email recipient
     * @param subject - email subject
     * @param emailText - content of the email
     * @throws MessagingException - throws an exception if there was an error in sending email. Must be caught in referenced classes.
     */
    public void sendEmail(String emailTo, String subject, String emailText) throws MessagingException{
        //Get system properties
        try {
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard().withCredentials(new AWSCredentialsProvider() {
                        @Override
                        public AWSCredentials getCredentials() {
                           return new AWSCredentials(){
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
            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("The email was not sent. Error message: "
                    + ex.getMessage());
        }
        System.out.println("Sent email with subject \"" + subject + "\" to \""+ emailTo + "\" successfully.");
    }
}
