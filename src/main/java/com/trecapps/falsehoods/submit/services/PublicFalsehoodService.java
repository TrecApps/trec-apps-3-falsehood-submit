package com.trecapps.falsehoods.submit.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.trecapps.base.FalsehoodModel.models.*;
import com.trecapps.falsehoods.submit.repos.PublicFalsehoodRecordsRepo;
import com.trecapps.falsehoods.submit.repos.PublicFalsehoodRepo;
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
public class PublicFalsehoodService {

    @Autowired
    PublicFalsehoodRepo pfRepo;

    @Autowired
    PublicFalsehoodRecordsRepo cRepos;

    @Autowired
    StorageClient storageClient;

    public Mono<String> submitFalsehood(FullPublicFalsehood full, String subject)
    {
        String contents = full.getContents();
        PublicFalsehood falsehood = full.getMetadata();
        if(contents == null || falsehood == null)
            return Mono.just("400: Missing details!");

        falsehood.setId(null);
        falsehood.setUserId(subject);
        falsehood = pfRepo.save(falsehood);
        BigInteger fId = falsehood.getId();
        return storageClient.SubmitDocument("PublicFalsehood-" + fId, contents, subject)
                .map((String str) ->{
                    List<Record> records = new ArrayList<>();
                    records.add(new Record("Event", "Creation", new Date(Calendar.getInstance().getTime().getTime()), 0l, null));

                    PublicFalsehoodRecords newRecords = new PublicFalsehoodRecords(fId,
                            (byte)fId.divideAndRemainder(BigInteger.valueOf(20))[1].intValue(), records);

                    try {
                        cRepos.save(newRecords);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                        return "Error In Json Processing!";
                    }
                    return "";
                });

    }

    public String editFalsehoodMetadata(PublicFalsehood falsehood, String comment)
    {
        BigInteger fId = falsehood.getId();
        if(!pfRepo.existsById(fId))
            return "404: Falsehood not documented!";

        PublicFalsehood currentFalsehood = pfRepo.getById(fId);
        if(!currentFalsehood.getUserId().equals(falsehood.getUserId()))
            return "400: Cannot attempt to change the id of the submitter on the Falsehood!";

        falsehood = pfRepo.save(falsehood);

        try {
            PublicFalsehoodRecords records = new PublicFalsehoodRecords(fId, (byte)1, cRepos.retrieveRecords(fId));

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

        PublicFalsehood metadata = pfRepo.getById(id);
        if(!principal.getSubject().equals(metadata.getUserId()))
            return Mono.just("401: Only the Owner of the Falsehood can change the contents");

        return storageClient.SubmitDocument("PublicFalsehood-" + metadata.getId(), contents, principal.getSubject())
                .map((String str) -> {
                    try {
                        PublicFalsehoodRecords records = new PublicFalsehoodRecords(id, (byte)1, cRepos.retrieveRecords(id));

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
