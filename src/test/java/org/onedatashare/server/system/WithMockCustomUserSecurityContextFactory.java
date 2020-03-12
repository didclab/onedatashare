package org.onedatashare.server.system;

import edu.emory.mathcs.backport.java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.security.Principal;

/**
 * A security context factory that instantiates a {@link SecurityContext} object that holds information
 * about the {@link Principal} and the custom method of {@link Authentication} to be used.
 * This factory links up the {@link WithMockCustomUser} annotation to the Spring boot test where it the annotation
 * is used
 */
public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {

    /**
     * Creates the {@link SecurityContext} to be used in tests to hold information about the user
     * identified by the fields provided in the {@link WithMockCustomUser} annotation
     *
     * @param customUser The annotation which contains information about the user
     * @return a customized security context
     */
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String principal = customUser.username();
        SimpleGrantedAuthority role = new SimpleGrantedAuthority(customUser.role().toString());
        Authentication auth =
                new UsernamePasswordAuthenticationToken(principal,
                        customUser.password(),
                        Collections.singletonList(role));
        context.setAuthentication(auth);
        return context;
    }
}
