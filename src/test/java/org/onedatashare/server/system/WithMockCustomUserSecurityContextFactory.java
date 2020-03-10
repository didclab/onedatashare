package org.onedatashare.server.system;

import edu.emory.mathcs.backport.java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {
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
