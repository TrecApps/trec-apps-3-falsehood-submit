package com.trecapps.falsehoods.submit.repos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trecapps.falsehoods.submit.config.StorageClient;
import com.trecapps.falsehoods.submit.models.Record;
import com.trecapps.falsehoods.submit.models.RegionRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class RegionRecordsRepo {

    @Autowired
    StorageClient client;

    ObjectMapper mapper = new ObjectMapper();

    public void save(RegionRecords records) throws JsonProcessingException {
        if(records.getRegionId() == null)
            throw new NullPointerException("Null Region Id Provided!");

        String name = "Region-Records-" + records.getRegionId();

        client.SubmitJson(name, mapper.writeValueAsString(records.getRecords()));
    }

    public List<Record> retrieveRecords(long id) throws JsonProcessingException {
        String name = "Region-Records-" + id;

        String contents = client.getContents(name, "Resource").getBody();

        return mapper.readValue(contents, new TypeReference<List<Record>>() {
        });
    }


}
