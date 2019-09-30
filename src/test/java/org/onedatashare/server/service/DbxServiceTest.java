package org.onedatashare.server.service;

import org.junit.Before;
import org.junit.jupiter.api.*;
import org.onedatashare.server.model.useraction.UserAction;
import org.onedatashare.server.module.dropbox.DbxResource;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dropbox Service ")
class DbxServiceTest {
    ResourceService resourceService;
    DbxService dbxService;


    @Before
    public void setUp() throws Exception {
//        this.dbxService = new ResourceService<DbxResource>();
    }


    @Autowired
    private UserService userService;


    @Autowired
    private UserAction userAction;

    @Autowired
    private JobService jobService;


    @BeforeAll
    // TODO: Initialize with a valid user and dropbox token
    public static void initialize(){

    }

    @AfterAll
    // TODO: Delete the user and remote the token
    public static void cleanUp(){

    }

    @Test
    @DisplayName("testing list")
    public void listTest() throws Exception{
        String cookie = "user=ashish;hash=123;";
        userAction= null;
        assertNotNull(dbxService.list(cookie, userAction));

    }

    @Test
    @DisplayName("testing mkdir")
    public void mkdirTest(){

    }

    @Test
    @DisplayName("testing delete")
    public void deleteTest(){

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