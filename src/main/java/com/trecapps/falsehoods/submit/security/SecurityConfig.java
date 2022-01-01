package com.trecapps.falsehoods.submit.security;

import com.azure.spring.aad.webapp.AADOAuth2UserService;
import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.trecapps.falsehoods.submit.repos.FalsehoodUserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    AADOAuth2UserService getAadoAuth2UserService(AADAuthenticationProperties aadAuthProps)
    {
        return new AADOAuth2UserService(aadAuthProps);
    }

    @Autowired
    AADOAuth2UserService aadoAuth2UserService;

    @Autowired
    FalsehoodUserRepo falsehoodUserRepo;

    @Override
    protected void configure(HttpSecurity security) throws Exception
    {
        security
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .oidcUserService(getTrecDirectoryService());
    }

    @Bean
    protected TrecActiveDirectoryService getTrecDirectoryService()
    {
        return new TrecActiveDirectoryService(aadoAuth2UserService, falsehoodUserRepo);
    }

}
