package org.onedatashare.server.service;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HTTP file service ")
class HttpFileServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private JobService jobService;

    @BeforeAll
    // TODO: Initialize with a valid user
    public static void initialize(){

    }

    @AfterAll
    // TODO: Delete the user
    public static void cleanUp(){

    }

    @Test
    @DisplayName("testing list")
    public void listTest(){

    }

    @Test
    @DisplayName("testing download URL")
    public void getDownloadURLTest(){

    }

    @Test
    @DisplayName("testing submit")
    public void submitTest(){

    }

}