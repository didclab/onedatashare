/**
 * ##**************************************************************
 * ##
 * ## Copyright (C) 2018-2020, OneDataShare Team,
 * ## Department of Computer Science and Engineering,
 * ## University at Buffalo, Buffalo, NY, 14260.
 * ##
 * ## Licensed under the Apache License, Version 2.0 (the "License"); you
 * ## may not use this file except in compliance with the License.  You may
 * ## obtain a copy of the License at
 * ##
 * ##    http://www.apache.org/licenses/LICENSE-2.0
 * ##
 * ## Unless required by applicable law or agreed to in writing, software
 * ## distributed under the License is distributed on an "AS IS" BASIS,
 * ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * ## See the License for the specific language governing permissions and
 * ## limitations under the License.
 * ##
 * ##**************************************************************
 */

package org.onedatashare.server.system;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.onedatashare.server.controller.RegistrationController;
import org.onedatashare.server.model.core.Role;
import org.onedatashare.server.system.base.UserActionTest;
import org.onedatashare.server.system.mockuser.WithMockCustomUser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A system test suite that tests actions related to user registration and verification.
 * <br><br>
 * Entry point for requests: {@link RegistrationController}
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
public class RegistrationTest extends UserActionTest {

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenUserDoesNotExist_WhenRegistered_ShouldBeAddedToUserRepository() throws Exception {
        registerUserAndChangePassword(TEST_USER_EMAIL, TEST_USER_PASSWORD, TEST_USER_NAME);

        assertEquals(users.size(), 1);
        assertEquals((getFirstUser()).getEmail(), TEST_USER_EMAIL);
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenUserAlreadyExists_WhenRegistered_ShouldNotDuplicateUser() throws Exception {
        registerUserAndChangePassword(TEST_USER_EMAIL, TEST_USER_PASSWORD, TEST_USER_NAME);
        long firstRegistrationTimestamp = getFirstUser().getRegisterMoment();

        // Try registering the same user again
        registerUserAndChangePassword(TEST_USER_EMAIL, TEST_USER_PASSWORD, TEST_USER_NAME);

        assertEquals(users.size(), 1);
        assertEquals(getFirstUser().getEmail(), TEST_USER_EMAIL);
        assertEquals(getFirstUser().getRegisterMoment(), firstRegistrationTimestamp);
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenUserRegistered_WhenCodeIsVerified_ShouldValidateUser() throws Exception {
        registerUserAndChangePassword(TEST_USER_EMAIL, TEST_USER_PASSWORD, TEST_USER_NAME);

        assertTrue(users.get(TEST_USER_EMAIL).isValidated());
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenUserStillNotVerified_WhenResendingVerificationEmail_ShouldResend() throws Exception {
        registerUser(TEST_USER_EMAIL, TEST_USER_NAME);
        userInbox.clear();
        resendVerificationCode(TEST_USER_EMAIL);

        assertEquals(1, userInbox.size());
        assertNotNull(userInbox.get(TEST_USER_EMAIL));
    }

    @Test
    @WithMockCustomUser(username = TEST_USER_EMAIL, role = Role.USER)
    public void givenUserAlreadyVerified_WhenResendingVerificationEmail_ShouldFail() throws Exception {
        // this method verifies the user through email
        registerUserAndChangePassword(TEST_USER_EMAIL, TEST_USER_PASSWORD, TEST_USER_NAME);
        userInbox.clear();
        resendVerificationCode(TEST_USER_EMAIL);

        assertEquals(0, userInbox.size());
    }

}
