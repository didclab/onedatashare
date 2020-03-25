/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


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

