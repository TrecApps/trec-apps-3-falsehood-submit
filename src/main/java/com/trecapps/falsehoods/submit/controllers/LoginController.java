package com.trecapps.falsehoods.submit.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trecapps.auth.models.LoginToken;
import com.trecapps.auth.models.TcUser;
import com.trecapps.auth.models.TrecAuthentication;
import com.trecapps.auth.models.primary.TrecAccount;
import com.trecapps.auth.services.JwtTokenService;
import com.trecapps.auth.services.TrecAccountService;
import com.trecapps.auth.services.UserStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Auth")
public class LoginController {
    @Autowired
    TrecAccountService authService;

    @Autowired
    JwtTokenService jwtTokenService;

    @Autowired
    UserStorageService userStorageService;

    Logger logger = LoggerFactory.getLogger(LoginController.class);

    @PostMapping("/login")
    public ResponseEntity<LoginToken> login(@RequestBody Login login)
    {
        logger.info("In Login Controller");
//        if(!login.getUsername().endsWith(url))
//            login.setUsername(login.getUsername() + '@' + url);
//        return generateResponse(authService.(login).getBody());
        TrecAccount account = authService.logInUsername(login.getUsername(), login.getPassword());

        if(account == null) {
            logger.info("Null account detected!");
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        String userToken = jwtTokenService.generateToken(account, null);
        String refreshToken = jwtTokenService.generateRefreshToken(account);

        logger.info("User {} logging in as {}", login.getUsername(), account.getId());

        if(userToken == null)
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);


        LoginToken ret = new LoginToken();
        ret.setToken_type("User");
        ret.setAccess_token(userToken);
        ret.setRefresh_token(refreshToken);

        SecurityContext secContext = SecurityContextHolder.createEmptyContext();
        TrecAuthentication tAuth = new TrecAuthentication(account);
        tAuth.setLoginToken(ret);
        secContext.setAuthentication(tAuth);
        SecurityContextHolder.setContext(secContext);
        return new ResponseEntity<>(ret, HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity getUser() {
        TrecAuthentication trecAuth = (TrecAuthentication) SecurityContextHolder.getContext().getAuthentication();
        logger.info("Trec Account Detected: {}", trecAuth.getAccount().getId());
        try {
            return new ResponseEntity<>(userStorageService.retrieveUser(trecAuth.getAccount().getId()), HttpStatus.OK);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<String>("Failure on Back end!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
