package com.andreitraistaru.cloudfilestorage.service;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class S3ClientService {
    @Value("${cloud.storage.service.s3.bucketName}")
    private String s3BucketName;
    private final AmazonS3 s3client;

    public S3ClientService(@Value("${cloud.storage.service.s3.accessKey}") String accessKey,
                           @Value("${cloud.storage.service.s3.secretKey}") String secretKey) {
        this.s3client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
    }

    public String uploadFile(String fileName, MultipartFile multipartFile) throws IOException {
        File file = convertMultiPartToFile(multipartFile, fileName);

        PutObjectResult result = s3client.putObject(new PutObjectRequest(s3BucketName, fileName, file));

        file.delete();

        return result.getVersionId();
    }

    public Resource downloadFile(String fileName, String versionId) {
        S3Object s3Object;

        if (versionId != null) {
            s3Object = s3client.getObject(new GetObjectRequest(s3BucketName, fileName, versionId));
        } else {
            s3Object = s3client.getObject(new GetObjectRequest(s3BucketName, fileName));
        }

        return convertS3ObjectToResource(s3Object);
    }

    public void deleteFile(String fileName) {
        s3client.deleteObject(new DeleteObjectRequest(s3BucketName, fileName));
    }

    public List<String> listFiles() {
        return s3client.listObjects(s3BucketName)
                .getObjectSummaries()
                .stream()
                .map(S3ObjectSummary::getKey)
                .toList();
    }
    public boolean checkFileExists(String fileName) {
        return s3client.doesObjectExist(s3BucketName, fileName);
    }

    private File convertMultiPartToFile(MultipartFile multipartFile, String fileName) throws IOException {
        File file = new File(fileName);

        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(multipartFile.getBytes());
        fileOutputStream.close();

        return file;
    }

    private Resource convertS3ObjectToResource(S3Object s3Object) {
        return new InputStreamResource(s3Object.getObjectContent());
    }
}
