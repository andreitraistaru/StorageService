package com.andreitraistaru.storageservice.service;

import com.andreitraistaru.storageservice.exception.AlreadyExistingStorageItemException;
import com.andreitraistaru.storageservice.exception.MissingStorageItemException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageInterface {
    String createFile(String fileName, MultipartFile multipartFile) throws AlreadyExistingStorageItemException;

    String updateFile(String fileName, MultipartFile multipartFile) throws MissingStorageItemException;

    Resource downloadFile(String fileName, String versionId) throws MissingStorageItemException;

    void deleteFile(String fileName) throws MissingStorageItemException;

    long getNumberOfFiles();

    List<String> getFilesMatchingRegexp(String regexp);
}
