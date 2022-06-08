package com.trecapps.falsehoods.submit.controllers;

import com.trecapps.auth.models.LoginToken;
import com.trecapps.auth.models.TrecAuthentication;
import com.trecapps.auth.models.primary.TrecAccount;
import com.trecapps.auth.services.JwtTokenService;
import com.trecapps.auth.services.TrecAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Auth")
public class LoginController {
    @Autowired
    TrecAccountService authService;

    @Autowired
    JwtTokenService jwtTokenService;

    @PostMapping("/login")
    public ResponseEntity<LoginToken> login(@RequestBody Login login)
    {
//        if(!login.getUsername().endsWith(url))
//            login.setUsername(login.getUsername() + '@' + url);
//        return generateResponse(authService.(login).getBody());
        TrecAccount account = authService.logInUsername(login.getUsername(), login.getPassword());

        if(account == null)
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        String userToken = jwtTokenService.generateToken(account, null);
        String refreshToken = jwtTokenService.generateRefreshToken(account);

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
}
