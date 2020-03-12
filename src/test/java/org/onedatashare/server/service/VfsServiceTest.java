package org.onedatashare.server.service;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onedatashare.server.model.core.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VFS Service ")
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VfsServiceTest {
    private static final String email = "vfsTestUser@gmail.com", password = "password", firstName = "VFS", lastName = "User", organization = "testers";

    @Autowired
    private static UserService userService;

    @Autowired
    private static JobService jobService;

    @BeforeAll
    // TODO: Initialize with a valid user and credentials
    public static void initialize(){
        assertNotNull(userService);
        assertNotNull(jobService);
        userService.register(email, firstName, lastName, organization, "");
        User user = userService.getUser(email).block();
        System.out.println(user);
    }

    @AfterAll
    // TODO: Delete the user and credentials
    public static void cleanUp(){
    }

    @Test
    @DisplayName("testing list")
    public void listTest(){

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