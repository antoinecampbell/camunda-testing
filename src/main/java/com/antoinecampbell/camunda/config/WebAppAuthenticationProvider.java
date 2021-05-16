package com.antoinecampbell.camunda.config;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult;
import org.camunda.bpm.engine.rest.security.auth.impl.ContainerBasedAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class WebAppAuthenticationProvider extends ContainerBasedAuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(WebAppAuthenticationProvider.class.getName());

    @Override
    public AuthenticationResult extractAuthenticatedUser(HttpServletRequest request, ProcessEngine engine) {

        logger.info("++ WebAppAuthenticationProvider.extractAuthenticatedUser()....");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.debug("++ authentication == null...return unsuccessful.");
            return AuthenticationResult.unsuccessful();
        }

        logger.debug("++ authentication IS NOT NULL");
        String name = authentication.getName();
        if (name == null || name.isEmpty()) {
            return AuthenticationResult.unsuccessful();
        }

        logger.debug("++ name = " + name);
        AuthenticationResult authenticationResult = new AuthenticationResult(name, true);
        authenticationResult.setGroups(getUserGroups(authentication));

        return authenticationResult;
    }

    private List<String> getUserGroups(Authentication authentication) {

        logger.info("++ WebAppAuthenticationProvider.getUserGroups()....");
        List<String> groupIds = Collections.emptyList();

        if (authentication instanceof OAuth2AuthenticationToken) {
            Object groups = ((OAuth2AuthenticationToken) authentication).getPrincipal().getAttributes().get("groups");
            if (groups instanceof List) {
                groupIds = ((List<?>) groups).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }
        }

        logger.debug("++ groupIds = " + groupIds.toString());

        return groupIds;

    }

}