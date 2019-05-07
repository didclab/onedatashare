package org.onedatashare.server.service;

import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

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
    final String EMAIL_PWD = System.getenv("ODS_EMAIL_PWD");

    /**
     * Method that creates and sends an email
     *
     * @param emailTo - email recipient
     * @param subject - email subject
     * @param emailText - content of the email
     * @throws MessagingException - throws an exception if there was an error in sending email. Must be caught in referenced classes.
     */
    public void sendEmail(String emailTo, String subject, String emailText) throws MessagingException{
        // Get system properties
        Properties properties = System.getProperties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PWD);
            }
        });

        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);
        // Set From: header field of the header.
        message.setFrom(new InternetAddress(EMAIL_USERNAME));
        // Set To: header field of the header.
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
        // Set Subject: header field
        message.setSubject(subject);
        // Now set the actual message
        message.setText(emailText);
        // Send message
        Transport.send(message);
        System.out.println("Sent email with subject \"" + subject + "\" to \""+ emailTo + "\" successfully.");
    }
}
