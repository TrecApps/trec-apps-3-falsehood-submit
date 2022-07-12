package com.trecapps.falsehoods.submit.repos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trecapps.base.FalsehoodModel.models.PublicFalsehoodRecords;
import com.trecapps.base.InfoResource.models.Record;
import com.trecapps.falsehoods.submit.config.StorageClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;

@Component
public class PublicFalsehoodRecordsRepo// extends CosmosRepository<PublicFalsehoodRecords, BigInteger>
{
    @Autowired
    StorageClient client;

    ObjectMapper mapper = new ObjectMapper();

    public void save(PublicFalsehoodRecords records) throws JsonProcessingException {
        if(records.getFalsehoodId() == null)
            throw new NullPointerException("Null Falsehood Id Provided!");

        String name = "Public-Falsehood-Records-" + records.getFalsehoodId();

        client.SubmitJson(name, mapper.writeValueAsString(records.getRecords()));
    }

    public List<Record> retrieveRecords(BigInteger id) throws JsonProcessingException {
        String name = "Public-Falsehood-Records-" + id;

        String contents = client.getContents(name, "Falsehood").getBody();

        return mapper.readValue(contents, new TypeReference<List<Record>>() {
        });
    }
}
