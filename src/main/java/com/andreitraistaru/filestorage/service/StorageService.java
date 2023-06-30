package com.andreitraistaru.filestorage.service;

import com.andreitraistaru.filestorage.exceptions.AlreadyExistingStorageItemException;
import com.andreitraistaru.filestorage.exceptions.MissingStorageItemException;
import com.andreitraistaru.filestorage.exceptions.StorageCorruptionFoundException;
import com.andreitraistaru.filestorage.exceptions.StorageServiceException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

@Service
@Log4j2
public class StorageService {
    @Value("${storage.service.root.path}")
    private String rootPath;

    public StorageService(@Value("${storage.service.root.path}") String rootPath) throws IOException {
        Files.createDirectories(Files.createDirectories(Paths.get(rootPath)));
    }

    private File getFileForStorageItem(String storageItemName) {
        return new File(rootPath.concat(storageItemName.replace("", "/").concat(storageItemName)));
    }

    public boolean checkIfStorageItemExists(File storageItemFile) throws StorageCorruptionFoundException {
        if (storageItemFile.exists()) {
            if (storageItemFile.isDirectory()) {
                log.error("Storage service found a directory when searching for item at " +
                        storageItemFile.getAbsolutePath() + ". Storage might have been corrupted.");
                log.info("Recovering Storage started: ...");
                log.info("Deleting directory " + storageItemFile.getAbsolutePath());

                if (!deleteRecursively(storageItemFile)) {
                    log.error("Recovering Storage failed! Fix it manually.");

                    throw new StorageCorruptionFoundException();
                } else {
                    log.info("Recovering Storage finished successfully.");
                }

                return false;
            } else {
                return true;
            }
        }

        return false;
    }

    public void createStorageItem(String storageItemName, MultipartFile storageItemMultipartFile) throws
            StorageServiceException {
        File storageItemFileForPersistence = getFileForStorageItem(storageItemName);
        try {
            if (checkIfStorageItemExists(storageItemFileForPersistence)) {
                throw new AlreadyExistingStorageItemException();
            }
        } catch (StorageCorruptionFoundException e) {
            log.error(Arrays.toString(e.getStackTrace()));

            throw new StorageServiceException();
        }

        storageItemFileForPersistence.getParentFile().mkdirs();

        try (OutputStream outputStream = new FileOutputStream(storageItemFileForPersistence)) {
            outputStream.write(storageItemMultipartFile.getBytes());
        } catch (IOException e) {
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    public void deleteStorageItem(String storageItemName) throws
            StorageCorruptionFoundException, MissingStorageItemException {
        File storageItemFileForPersistence = getFileForStorageItem(storageItemName);
        try {
            if (!checkIfStorageItemExists(storageItemFileForPersistence)) {
                throw new MissingStorageItemException();
            }
        } catch (StorageCorruptionFoundException ignored) {
            // We were lucky this time that the user also no longer need this file.
            // Handling this as a successful request masking an internal issue that
            // has no user impact in this case.
            return;
        }

        if (!storageItemFileForPersistence.delete()) {
            throw new StorageCorruptionFoundException();
        }
    }

    private boolean deleteRecursively(File file) {
        if (!file.exists()) {
            return true;
        }

        if (file.isFile()) {
            if (!file.delete()) {
                log.warn(file.getAbsolutePath() + " could not be deleted. Skipping it...");

                return false;
            }

            return true;
        }

        File[] childFiles = file.listFiles();

        if (childFiles != null) {
            for (File childFile : childFiles) {
                deleteRecursively(childFile);
            }
        }

        return file.delete();
    }
}
