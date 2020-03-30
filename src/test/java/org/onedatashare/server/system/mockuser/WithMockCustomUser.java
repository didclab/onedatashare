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



package org.onedatashare.server.system.mockuser;

import org.onedatashare.server.model.core.Role;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An annotation to be used in order to specify to Spring's {@link ReactiveSecurityContextHolder} which user
 * is currently logged in. This annotation can be used to authenticate a user identified by the provided fields
 * to the Spring security framework.
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

        /**
         * The username (handle) that the user uses to sign in to their account
         *
         * @return the username
0         */
        String username() default "test_user";

        /**
         *The password that the user uses to log in to their account
         *
         * @return the password
         */
        String password() default "test_password";

        /**
         * The {@link Role} with which this user is associated
         *
         * @return the intended role
         */
        Role role() default Role.USER;
}
