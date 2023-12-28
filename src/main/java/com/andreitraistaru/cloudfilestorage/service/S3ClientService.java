package com.andreitraistaru.cloudfilestorage.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

@Service
public class S3ClientService {
    private final S3Client s3Client;
    private final String bucketName;

    public S3ClientService(@Value("${cloud.storage.service.s3.bucket}") String s3BucketName) {
        this.s3Client = S3Client.builder().build();
        this.bucketName = s3BucketName;
    }

    public void uploadFile(String fileName, InputStream inputStream) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(inputStream, inputStream.available()));
    }
}
