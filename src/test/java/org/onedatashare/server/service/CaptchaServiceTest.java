package org.onedatashare.server.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.util.Map;


public class CaptchaServiceTest {

    /**
     * Google provided this secret key for testing purposes. A call to the verify API using
     * this key always results in success
     */
    public static final String FAKE_SECRET_KEY = "6LeIxAcTAAAAAGG-vFI1TnRWxMZNFuojJ4WifJWe";
    public static final String GOOGLE_CAPTCHA_SECRET_KEY_VAR_NAME = "GOOGLE_CAPTCHA_SECRET";
    private CaptchaService captchaService;

    @Before
    public void setup() {
        this.captchaService = new CaptchaService();
    }

    @Test
    public void givenBlankResponse_WhenVerified_ShouldReturnFalse() {
        this.captchaService.verifyValue(null)
                .subscribe(
                        Assertions::assertFalse,
                        Assertions::fail
                );
    }

    @Test
    public void givenIncorrectResponse_WhenVerified_ShouldReturnFalse() {
        this.captchaService.verifyValue("bogus")
                .subscribe(
                        Assertions::assertFalse,
                        Assertions::fail
                );
    }

    @Test
    public void givenCorrectResponse_WhenVerified_ShouldReturnTrue() {
        String prevSecretKey = System.getenv(GOOGLE_CAPTCHA_SECRET_KEY_VAR_NAME);
        changeEnvVar(GOOGLE_CAPTCHA_SECRET_KEY_VAR_NAME, FAKE_SECRET_KEY);
        this.captchaService = new CaptchaService();
        this.captchaService.verifyValue("right_input")
                .subscribe(
                        Assertions::assertTrue,
                        Assertions::fail
                );
        changeEnvVar(GOOGLE_CAPTCHA_SECRET_KEY_VAR_NAME, prevSecretKey);
    }

    private void changeEnvVar(String key, String value) {
            try {
                Map<String, String> env = System.getenv();
                Class<?> cl = env.getClass();
                Field field = cl.getDeclaredField("m");
                field.setAccessible(true);
                Map<String, String> writableEnv = (Map<String, String>) field.get(env);
                writableEnv.put(key, value);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to set environment variable", e);
            }
        }
    }

