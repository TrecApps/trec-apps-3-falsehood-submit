//package com.trecapps.falsehoods.submit.security;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//
//@EnableWebSecurity
//@Order(1)
//public class SecurityAnonConfig extends WebSecurityConfigurerAdapter {
//    @Override
//    protected void configure(HttpSecurity security) throws Exception {
//        security.csrf().disable().requestMatchers().antMatchers("/Auth/login").anyRequest()
//                .and().authorizeRequests().anyRequest().permitAll();
//    }
//}
