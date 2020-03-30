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
