package com.trecapps.falsehoods.submit.repos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trecapps.falsehoods.submit.config.StorageClient;
import com.trecapps.falsehoods.submit.models.PublicFigureRecords;
import com.trecapps.falsehoods.submit.models.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PublicFigureRecordRepo {
    @Autowired
    StorageClient client;

    ObjectMapper mapper = new ObjectMapper();

    public void save(PublicFigureRecords records) throws JsonProcessingException {
        if(records.getFigureId() == null)
            throw new NullPointerException("Null Public-Figure Id Provided!");

        String name = "Public-Figure-Records-" + records.getFigureId();

        client.SubmitJson(name, mapper.writeValueAsString(records.getRecords()));
    }

    public List<Record> retrieveRecords(long id) throws JsonProcessingException {
        String name = "Public-Figure-Records-" + id;

        String contents = client.getContents(name, "Resource").getBody();

        return mapper.readValue(contents, new TypeReference<List<Record>>() {
        });
    }
}
