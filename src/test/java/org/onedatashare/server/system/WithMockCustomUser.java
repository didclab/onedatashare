package org.onedatashare.server.system;

import org.onedatashare.server.model.core.Role;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory.class)
public @interface WithMockCustomUser {

        String username() default "test_user";

        String password() default "test_password";

        Role role() default Role.USER;
}
