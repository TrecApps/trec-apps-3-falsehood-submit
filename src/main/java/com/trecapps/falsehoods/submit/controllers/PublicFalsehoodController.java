package com.trecapps.falsehoods.submit.controllers;

import com.trecapps.auth.models.TcUser;
import com.trecapps.auth.services.UserStorageService;
import com.trecapps.base.FalsehoodModel.models.Falsehood;
import com.trecapps.base.FalsehoodModel.models.FalsehoodUser;
import com.trecapps.base.FalsehoodModel.models.FullPublicFalsehood;
import com.trecapps.base.FalsehoodModel.models.PublicFalsehood;
import com.trecapps.falsehoods.submit.services.PublicFalsehoodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

@RestController
@RequestMapping("/Update/Public")
public class PublicFalsehoodController extends FalsehoodControllerBase{


    PublicFalsehoodService publicFalsehoodService;

    Logger logger = LoggerFactory.getLogger(PublicFalsehoodController.class);
    @Autowired
    public PublicFalsehoodController(UserStorageService userStorageService1,
                                     PublicFalsehoodService publicFalsehoodService1) {
        super(userStorageService1);
        publicFalsehoodService = publicFalsehoodService1;
    }

    @PostMapping("/Submit")
    public ResponseEntity<String> submitMediaFalsehood(RequestEntity<FullPublicFalsehood> falsehood)
    {
        logger.info("New Public Falsehood Submission: {}", falsehood.getBody().getMetadata());
        TcUser user = null;

        try {
            user = getUserDetails(SecurityContextHolder.getContext());
        }catch (Exception e)
        {
            return new ResponseEntity<String>
                    (e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(user.getCredibilityRating() < MIN_CREDIT_SUBMIT_NEW)
            return new ResponseEntity<String>
                    ("Your Credibility Is too low. Please wait until it is set to five before trying again!",
                            HttpStatus.FORBIDDEN);
        return super.getResult(publicFalsehoodService.submitFalsehood(falsehood.getBody(), user.getId()));
    }

    @PutMapping("/Metadata")
    public ResponseEntity<String> updateMetadata(RequestEntity<FullPublicFalsehood> falsehood)
    {
        logger.info("Updating Public Falsehood from Controller");
        FullPublicFalsehood obj = falsehood.getBody();
        PublicFalsehood metaData = obj.getMetadata();
        TcUser user = null;

        try {
            user = getUserDetails(SecurityContextHolder.getContext());
        }catch (Exception e)
        {
            return new ResponseEntity<String>
                    (e.getMessage(),
                            HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(!user.getId().equals(metaData.getUserId()) && user.getCredibilityRating() < MIN_CREDIT_UPDATE_METADATA)
            return new ResponseEntity<String>
                    ("Your Credibility needs to be 400 points or above to change the metadata of another user's Falsehood!",
                            HttpStatus.FORBIDDEN);
        return super.getResult(publicFalsehoodService.editFalsehoodMetadata(obj.getMetadata(), obj.getContents()));
    }

    @PutMapping(value = "/Content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateContents(RequestEntity<MultiValueMap<String, String>> request)
    {
        logger.info("Updating Contents of a Public Falsehood");
        MultiValueMap<String, String> values = request.getBody();
        BigInteger id = null;
        TcUser user = null;

        try {
            user = getUserDetails(SecurityContextHolder.getContext());
        }catch (Exception e)
        {
            return new ResponseEntity<String>
                    (e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        try
        {
            id = new BigInteger(values.getFirst("Falsehood"));
        } catch (Exception e)
        {
            return new ResponseEntity<String>("Could not derive an id from the Falsehood field!", HttpStatus.BAD_REQUEST);
        }

        return super.getResult(publicFalsehoodService.editFalsehoodContents(id,
                        values.getFirst("Contents"), values.getFirst("Reason"),user));
    }
}
