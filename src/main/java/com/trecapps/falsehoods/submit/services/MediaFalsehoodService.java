package com.trecapps.falsehoods.submit.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trecapps.auth.models.TcUser;
import com.trecapps.falsehoods.submit.models.Falsehood;
import com.trecapps.falsehoods.submit.models.FalsehoodRecords;
import com.trecapps.falsehoods.submit.models.FullFalsehood;
import com.trecapps.falsehoods.submit.models.Record;
import com.trecapps.falsehoods.submit.repos.FalsehoodRecordsRepo;
import com.trecapps.falsehoods.submit.repos.FalsehoodRepo;
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
public class MediaFalsehoodService {

    @Autowired
    FalsehoodRepo pfRepo;

    @Autowired
    FalsehoodRecordsRepo cRepos;

    @Autowired
    StorageClient storageClient;

    Logger logger = LoggerFactory.getLogger(MediaFalsehoodService.class);

    public String submitFalsehood(FullFalsehood full, String subject)
    {
        String contents = full.getContents();
        Falsehood falsehood = full.getMetadata();
        if(contents == null || falsehood == null)
            return "400: Missing details!";

        falsehood.setId(null);
        falsehood.setUserId(subject);
        falsehood = pfRepo.save(falsehood);
        BigInteger fId = falsehood.getId();

        storageClient.SubmitDocument("MediaFalsehood-" + fId, contents, subject);

        List<Record> records = new ArrayList<>();
        records.add(new Record("Event", "Creation", new Date(Calendar.getInstance().getTime().getTime()), 0l, null));


        try {
            cRepos.save(new FalsehoodRecords(fId,
            (byte)fId.divideAndRemainder(BigInteger.valueOf(20))[1].intValue(), records));
        } catch (JsonProcessingException e) {
            logger.error("Error detected in JSON data in Media Falsehood Submission", e);
            return "Error in JSON Records!";
        }

        return "";
    }

    public String editFalsehoodMetadata(Falsehood falsehood, String comment)
    {
        BigInteger fId = falsehood.getId();
        if(!pfRepo.existsById(fId))
            return "404: Falsehood not documented!";

        Falsehood currentFalsehood = pfRepo.getById(fId);
        if(!currentFalsehood.getUserId().equals(falsehood.getUserId()))
            return "400: Cannot attempt to change the id of the submitter on the Falsehood!";

        logger.info("Changing metadata of Media Falsehood: {} --> {}", currentFalsehood, falsehood);

        falsehood = pfRepo.save(falsehood);

        try {
            FalsehoodRecords records = new FalsehoodRecords(fId, (byte)1, cRepos.retrieveRecords(fId));

            records.getRecords().add(new Record("Event", "Update", new Date(Calendar.getInstance().getTime().getTime()), 0l, comment));
            cRepos.save(records);
        } catch(JsonProcessingException ex)
        {
            logger.error("JSON Processing error occurred (Media Metadata):", ex);
            return "Json Processing Error Occurred!";
        }
        logger.info("Media Falsehood entry {} successfully updated", fId);
        return "";
    }

    public String editFalsehoodContents(BigInteger id, String contents, String comment, TcUser principal)
    {
        if(!pfRepo.existsById(id))
            return "404: Falsehood not documented!";

        Falsehood metadata = pfRepo.getById(id);
        if(!principal.getId().equals(metadata.getUserId()))
            return "401: Only the Owner of the Falsehood can change the contents";

        storageClient.SubmitDocument("MediaFalsehood-" + metadata.getId(), contents, principal.getId());

        try {
            FalsehoodRecords records = new FalsehoodRecords(id, (byte)1, cRepos.retrieveRecords(id));

            records.getRecords().add(new Record("Event", "Edit", new Date(Calendar.getInstance().getTime().getTime()), 0l, comment));
            cRepos.save(records);
        } catch(JsonProcessingException ex)
        {
            logger.error("JSON Processing error occurred (Media Contents):", ex);
            return "Json Processing Error Occurred!";
        }
        logger.info("Media Falsehood contents {} successfully updated", id);
        return "";


    }
}
