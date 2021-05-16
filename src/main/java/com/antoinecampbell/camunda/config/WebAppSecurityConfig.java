package com.antoinecampbell.camunda.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@Order(SecurityProperties.BASIC_AUTH_ORDER - 15)
public class WebAppSecurityConfig extends WebSecurityConfigurerAdapter {

    private final Logger logger = LoggerFactory.getLogger(WebAppSecurityConfig.class.getName());

    private final OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

    public WebAppSecurityConfig(OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService) {
        this.oidcUserService = oidcUserService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests().antMatchers(/*"/app/admin/**",*/ "/app/cockpit/**", "/app/tasklist/**").authenticated()
                .and()
                .authorizeRequests().antMatchers("/**").permitAll()
                .and()
                .logout().logoutSuccessUrl("/")
                .and()
                .oauth2Client()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .oidcUserService(oidcUserService);


        http.csrf().disable();
    }

    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 15)
    public FilterRegistrationBean<CamundaAuthenticationFilter> containerBasedAuthenticationFilter() {

        logger.info("++++++++ WebAppSecurityConfig.containerBasedAuthenticationFilter()....");
        FilterRegistrationBean<CamundaAuthenticationFilter> filterRegistration
                = new FilterRegistrationBean<>();
        filterRegistration.setFilter(new CamundaAuthenticationFilter());
        filterRegistration.setInitParameters(Collections.singletonMap("authentication-provider", WebAppAuthenticationProvider.class.getName()));
        filterRegistration.setOrder(101); // make sure the filter is registered after the Spring Security Filter Chain
        filterRegistration.addUrlPatterns("/*");
        return filterRegistration;

    }

}