package com.andreitraistaru.storageservice.service;

import com.andreitraistaru.storageservice.exception.AlreadyExistingStorageItemException;
import com.andreitraistaru.storageservice.exception.MissingStorageItemException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class LocalFileStorage implements FileStorageInterface {
    @Value("${versioning.service.url}")
    private String versioningServiceUrl;
    @Value("${path.calculator.service.url}")
    private String pathCalculatorServiceUrl;

    public String createFile(String fileName, MultipartFile multipartFile) throws AlreadyExistingStorageItemException {
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromUriString("localhost:8081/file/create")
                .queryParam("fileName", fileName);

        RestTemplate restTemplate = new RestTemplate();
        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();

        parts.add("file", multipartFile.getResource());
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts, httpHeaders);

        ResponseEntity<String> response = restTemplate.postForEntity(urlBuilder.build().toString(), httpEntity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            throw new AlreadyExistingStorageItemException();
        }
    }

    public String updateFile(String fileName, MultipartFile multipartFile) throws MissingStorageItemException {
//        File file = convertMultiPartToFile(multipartFile, fileName);
//
//        PutObjectResult result = s3client.putObject(new PutObjectRequest(s3BucketName, fileName, file));
//
//        file.delete();
//
//        return result.getVersionId();
        return "";
    }

    public Resource downloadFile(String fileName, String versionId) throws MissingStorageItemException {
//        S3Object s3Object;
//
//        if (versionId != null) {
//            s3Object = s3client.getObject(new GetObjectRequest(s3BucketName, fileName, versionId));
//        } else {
//            s3Object = s3client.getObject(new GetObjectRequest(s3BucketName, fileName));
//        }
//
//        return convertS3ObjectToResource(s3Object);
        return null;
    }

    public void deleteFile(String fileName) throws MissingStorageItemException {
//        s3client.deleteObject(new DeleteObjectRequest(s3BucketName, fileName));
    }

    public long getNumberOfFiles() {
//        return s3client.listObjects(s3BucketName)
//                .getObjectSummaries()
//                .stream()
//                .map(S3ObjectSummary::getKey)
//                .toList();
        return 1;
    }
    public List<String> getFilesMatchingRegexp(String regexp) {
//        return s3client.doesObjectExist(s3BucketName, fileName);
        return null;
    }
}
