package com.trecapps.falsehoods.submit.config;


import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.trecapps.auth.services.UserStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class StorageClient {
    BlobServiceClient client;
    ObjectMapper objectMapper;
    Logger logger = LoggerFactory.getLogger(UserStorageService.class);
    public StorageClient(@Value("${trecapps.storage.account-name}") String name,
                         @Value("${trecapps.storage.account-key}") String key,
                         @Value("${trecapps.storage.blob-endpoint}") String endpoint,
                         Jackson2ObjectMapperBuilder objectMapperBuilder)
    {
        AzureNamedKeyCredential credential = new AzureNamedKeyCredential(name, key);
        this.client = (new BlobServiceClientBuilder()).credential(credential).endpoint(endpoint).buildClient();
        this.objectMapper = objectMapperBuilder.createXmlMapper(false).build();
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    public ResponseEntity<String> getContents(String id, String app)
    {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-falsehoods");
        BlobClient blobClient = containerClient.getBlobClient(id);
        return new ResponseEntity<>(blobClient.downloadContent().toString(), HttpStatus.OK);
    }

    public void SubmitDocument(String name, String contents, String account)
    {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-falsehoods");
        BlobClient blobClient = containerClient.getBlobClient(name);
        blobClient.upload(BinaryData.fromString(contents), true);
    }
    public void SubmitJson(String name, String contents)
    {
        BlobContainerClient containerClient = client.getBlobContainerClient("trec-apps-falsehoods");
        BlobClient blobClient = containerClient.getBlobClient(name + ".json");
        blobClient.upload(BinaryData.fromString(contents), true);
    }
}
