package com.trecapps.falsehoods.submit.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trecapps.base.FalsehoodModel.models.*;
import com.trecapps.base.FalsehoodModel.repos.FalsehoodRecordsRepo;
import com.trecapps.base.FalsehoodModel.repos.FalsehoodRepo;
import com.trecapps.base.InfoResource.models.Record;
import com.trecapps.base.InfoResource.config.StorageClient;
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

    public Mono<String> submitFalsehood(FullFalsehood full, String subject)
    {
        String contents = full.getContents();
        Falsehood falsehood = full.getMetadata();
        if(contents == null || falsehood == null)
            return Mono.just("400: Missing details!");

        falsehood.setId(null);
        falsehood.setUserId(subject);
        falsehood = pfRepo.save(falsehood);
        BigInteger fId = falsehood.getId();
        // To-Do: Set up Sotrage Client and send Contents of file to it

        return storageClient.SubmitDocument("MediaFalsehood-" + fId, contents, subject)
                .map((String str) -> {

                    List<Record> records = new ArrayList<>();
                    records.add(new Record("Event", "Creation", new Date(Calendar.getInstance().getTime().getTime()), 0l, null));


                    try {
                        cRepos.save(new FalsehoodRecords(fId,
                                (byte)fId.divideAndRemainder(BigInteger.valueOf(20))[1].intValue(), records));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return "Error in JSON Records!";
                    }

                    return "";
                });
    }

    public String editFalsehoodMetadata(Falsehood falsehood, String comment)
    {
        BigInteger fId = falsehood.getId();
        if(!pfRepo.existsById(fId))
            return "404: Falsehood not documented!";

        Falsehood currentFalsehood = pfRepo.getById(fId);
        if(!currentFalsehood.getUserId().equals(falsehood.getUserId()))
            return "400: Cannot attempt to change the id of the submitter on the Falsehood!";

        falsehood = pfRepo.save(falsehood);

        try {
            FalsehoodRecords records = new FalsehoodRecords(fId, (byte)1, cRepos.retrieveRecords(fId));

            records.getRecords().add(new Record("Event", "Update", new Date(Calendar.getInstance().getTime().getTime()), 0l, comment));
            cRepos.save(records);
        } catch(JsonProcessingException ex)
        {
            return "Json Processing Error Occurred!";
        }
        return "";
    }

    public Mono<String> editFalsehoodContents(BigInteger id, String contents, String comment, OidcUser principal)
    {
        if(!pfRepo.existsById(id))
            return Mono.just("404: Falsehood not documented!");

        Falsehood metadata = pfRepo.getById(id);
        if(!principal.getSubject().equals(metadata.getUserId()))
            return Mono.just("401: Only the Owner of the Falsehood can change the contents");

        return storageClient.SubmitDocument("MediaFalsehood-" + metadata.getId(), contents, principal.getSubject())
                .map((String str) -> {
                    try {
                        FalsehoodRecords records = new FalsehoodRecords(id, (byte)1, cRepos.retrieveRecords(id));

                        records.getRecords().add(new Record("Event", "Edit", new Date(Calendar.getInstance().getTime().getTime()), 0l, comment));
                        cRepos.save(records);
                    } catch(JsonProcessingException ex)
                    {
                        return "Json Processing Error Occurred!";
                    }
                    return "";
                });

    }
}
