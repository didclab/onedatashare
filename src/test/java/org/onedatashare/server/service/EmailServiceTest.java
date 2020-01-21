package org.onedatashare.server.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EmailServiceTest {

    EmailService emailService;

    String validInputEmail = "ods_test_user@test.com";
    String invalidInputEmail = "invalidEmail";

    @Before
    public void setup() {
        this.emailService = new EmailService();
    }

    @Test
    @DisplayName("Valid email")
    public void isValidEmail(){
        assertEquals(true,emailService.isValidEmail(validInputEmail),"Invalid email id");
    }

    @Test
    @DisplayName("InValid email")
    public void isInValidEmail(){
        assertEquals(false,emailService.isValidEmail(invalidInputEmail),"valid email id");
    }
}
