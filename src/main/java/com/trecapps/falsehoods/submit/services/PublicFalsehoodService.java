package com.trecapps.falsehoods.submit.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trecapps.auth.models.TcUser;

import com.trecapps.base.FalsehoodModel.models.FullPublicFalsehood;
import com.trecapps.base.FalsehoodModel.models.PublicFalsehood;
import com.trecapps.base.FalsehoodModel.models.PublicFalsehoodRecords;
import com.trecapps.base.InfoResource.models.Record;
import com.trecapps.falsehoods.submit.repos.PublicFalsehoodRecordsRepo;
import com.trecapps.falsehoods.submit.repos.PublicFalsehoodRepo;
import com.trecapps.falsehoods.submit.config.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Service
public class PublicFalsehoodService {

    @Autowired
    PublicFalsehoodRepo pfRepo;

    @Autowired
    PublicFalsehoodRecordsRepo cRepos;

    @Autowired
    StorageClient storageClient;

    Logger logger = LoggerFactory.getLogger(PublicFalsehoodService.class);

    public String submitFalsehood(FullPublicFalsehood full, String subject)
    {
        String contents = full.getContents();
        PublicFalsehood falsehood = full.getMetadata();
        if(contents == null || falsehood == null)
            return "400: Missing details!";

        falsehood.setId(null);
        falsehood.setUserId(subject);
        falsehood = pfRepo.save(falsehood);
        BigInteger fId = falsehood.getId();
        storageClient.SubmitDocument("PublicFalsehood-" + fId, contents, subject);

        List<Record> records = new ArrayList<>();
        records.add(new Record("Event", "Creation", new Date(Calendar.getInstance().getTime().getTime()), 0l, null));

        PublicFalsehoodRecords newRecords = new PublicFalsehoodRecords(fId,
            (byte)fId.divideAndRemainder(BigInteger.valueOf(20))[1].intValue(), records);

        try {
            cRepos.save(newRecords);
        } catch (JsonProcessingException e) {
            logger.error("Error detected in JSON data in Public Falsehood Submission", e);
            return "Error In Json Processing!";
        }
        return "";


    }

    public String editFalsehoodMetadata(PublicFalsehood falsehood, String comment)
    {
        BigInteger fId = falsehood.getId();
        if(!pfRepo.existsById(fId))
            return "404: Falsehood not documented!";

        PublicFalsehood currentFalsehood = pfRepo.getById(fId);
        if(!currentFalsehood.getUserId().equals(falsehood.getUserId()))
            return "400: Cannot attempt to change the id of the submitter on the Falsehood!";

        logger.info("Changing metadata of Public Falsehood: {} --> {}", currentFalsehood, falsehood);
        falsehood = pfRepo.save(falsehood);

        try {
            PublicFalsehoodRecords records = new PublicFalsehoodRecords(fId, (byte)1, cRepos.retrieveRecords(fId));

            records.getRecords().add(new Record("Event", "Update", new Date(Calendar.getInstance().getTime().getTime()), 0l, comment));
            cRepos.save(records);
        } catch(JsonProcessingException ex)
        {
            logger.error("JSON Processing error occurred (Public Metadata):", ex);
            return "Json Processing Error Occurred!";
        }
        logger.info("Public Falsehood metadata {} successfully updated", fId);
        return "";
    }

    public String editFalsehoodContents(BigInteger id, String contents, String comment, TcUser principal)
    {
        if(!pfRepo.existsById(id))
            return "404: Falsehood not documented!";

        PublicFalsehood metadata = pfRepo.getById(id);
        if(!principal.getId().equals(metadata.getUserId()))
            return "401: Only the Owner of the Falsehood can change the contents";

        storageClient.SubmitDocument("PublicFalsehood-" + metadata.getId(), contents, principal.getId());

        try {
            PublicFalsehoodRecords records = new PublicFalsehoodRecords(id, (byte)1, cRepos.retrieveRecords(id));

            records.getRecords().add(new Record("Event", "Edit", new Date(Calendar.getInstance().getTime().getTime()), 0l, comment));
            cRepos.save(records);
        } catch(JsonProcessingException ex)
        {
            logger.error("JSON Processing error occurred (Public Contents):", ex);
            return "Json Processing Error Occurred!";
        }
        logger.info("Public Falsehood contents {} successfully updated", id);
        return "";

    }
}
