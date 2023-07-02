package com.andreitraistaru.filestorage.service;

import com.andreitraistaru.filestorage.exceptions.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Log4j2
public class StorageService {
    private final String STORAGE_ITEM_EXTENSION = ".storage";
    private final String rootPath;
    private final int imbricationLevel;

    private final AtomicLong numberOfStorageItems = new AtomicLong();

    public StorageService(@Value("${storage.service.root.path}") String rootPath,
                          @Value("${storage.service.imbrication.level}") int imbricationLevel) throws IOException {
        if (rootPath.endsWith("/") || rootPath.endsWith("\\")) {
            this.rootPath = rootPath;
        } else {
            this.rootPath = rootPath.concat("/");
        }

        if (imbricationLevel <= 0) {
            log.error("Imbrication level must be > 0.");

            throw new RuntimeException();
        } else {
            this.imbricationLevel = imbricationLevel;
        }

        Path root = Paths.get(rootPath);
        Files.createDirectories(Files.createDirectories(root));

        this.numberOfStorageItems.set(computeNumberOfItemsInStorage(root));
    }

    private long computeNumberOfItemsInStorage(Path root) throws IOException {
        try (Stream<Path> filesWalking = Files.walk(root)) {
            return filesWalking.filter(path -> !Files.isDirectory(path)).count();
        }
    }

    private File getFileForStorageItem(String storageItemName) throws InvalidStorageItemNameException {
        if (storageItemName == null || storageItemName.isBlank() || storageItemName.length() > 64 || !storageItemName.matches("[a-zA-Z0-9_-]+")) {
            throw new InvalidStorageItemNameException();
        }

        StringBuilder rootPathForPersistence = new StringBuilder(rootPath);
        StringBuilder currentDirectoryName = new StringBuilder();
        int imbricationLevelsLeft = imbricationLevel;

        for (int i = 0; i < storageItemName.length(); i++) {
            currentDirectoryName.append(storageItemName.charAt(i));

            if (i % 2 == 1) {
                rootPathForPersistence.append(currentDirectoryName).append("/");
                currentDirectoryName = new StringBuilder();
                imbricationLevelsLeft--;

                if (imbricationLevelsLeft == 0) {
                    break;
                }
            }
        }

        rootPathForPersistence.append(storageItemName).append(STORAGE_ITEM_EXTENSION);

        return new File(rootPathForPersistence.toString());
    }

    private String getStorageItemNameFromFileName(String filename) {
        return filename.substring(0, filename.length() - STORAGE_ITEM_EXTENSION.length());
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

    public Resource readStorageItem(String storageItemName) throws StorageServiceException {
        File storageItemFileForPersistence = getFileForStorageItem(storageItemName);
        try {
            if (!checkIfStorageItemExists(storageItemFileForPersistence)) {
                throw new MissingStorageItemException();
            }
        } catch (StorageCorruptionFoundException e) {
            // Handling this as a missing storage item since most probably
            // the user hasn't created the file before and a directory with
            // the same name could have been created externally.
            throw new MissingStorageItemException();
        }

        try {
            return new UrlResource(storageItemFileForPersistence.toPath().toUri());
        } catch (MalformedURLException e) {
            log.error(Arrays.toString(e.getStackTrace()));

            throw new StorageServiceException();
        }
    }

    public void createStorageItem(String storageItemName, MultipartFile storageItemMultipartFile) throws
            StorageServiceException {
        File storageItemFileForPersistence = getFileForStorageItem(storageItemName);
        try {
            if (checkIfStorageItemExists(storageItemFileForPersistence)) {
                throw new AlreadyExistingStorageItemException();
            }
        } catch (StorageCorruptionFoundException e) {
            throw new StorageServiceException();
        }

        storageItemFileForPersistence.getParentFile().mkdirs();

        try (OutputStream outputStream = new FileOutputStream(storageItemFileForPersistence)) {
            outputStream.write(storageItemMultipartFile.getBytes());

            numberOfStorageItems.incrementAndGet();
        } catch (IOException e) {
            log.error(Arrays.toString(e.getStackTrace()));

            throw new StorageServiceException();
        }
    }

    public void updateStorageItem(String storageItemName, MultipartFile storageItemMultipartFile) throws
            StorageServiceException {
        File storageItemFileForPersistence = getFileForStorageItem(storageItemName);
        try {
            if (!checkIfStorageItemExists(storageItemFileForPersistence)) {
                throw new MissingStorageItemException();
            }
        } catch (StorageCorruptionFoundException e) {
            // Handling this as a missing storage item since most probably
            // the user hasn't created the file before and a directory with
            // the same name could have been created externally.
            throw new MissingStorageItemException();
        }

        try (OutputStream outputStream = new FileOutputStream(storageItemFileForPersistence)) {
            outputStream.write(storageItemMultipartFile.getBytes());
        } catch (IOException e) {
            log.error(Arrays.toString(e.getStackTrace()));

            throw new StorageServiceException();
        }
    }

    public void deleteStorageItem(String storageItemName) throws
            StorageCorruptionFoundException, MissingStorageItemException, InvalidStorageItemNameException {
        File storageItemFileForPersistence = getFileForStorageItem(storageItemName);
        try {
            if (!checkIfStorageItemExists(storageItemFileForPersistence)) {
                throw new MissingStorageItemException();
            }
        } catch (StorageCorruptionFoundException ignored) {
            // Handling this as a missing storage item since most probably
            // the user hasn't created the file before and a directory with
            // the same name could have been created externally.
            throw new MissingStorageItemException();
        }

        if (!storageItemFileForPersistence.delete()) {
            throw new StorageCorruptionFoundException();
        } else {
            numberOfStorageItems.decrementAndGet();
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

    public long getTotalNumberOfItemsInStorage() {
        return numberOfStorageItems.get();
    }

    public List<String> getStorageItemsMatchingRegexp(String regexp) throws StorageServiceException {
        if (regexp == null || regexp.isBlank()) {
            throw new InvalidRegexpException();
        }

        List<String> itemsMatchingRegexp;

        try (Stream<Path> filesWalking = Files.walk(Paths.get(rootPath))) {
            itemsMatchingRegexp = filesWalking
                    .filter(path -> !Files.isDirectory(path))
                    .map(path -> getStorageItemNameFromFileName(path.getFileName().toString()))
                    .filter(filename -> filename.matches(regexp))
                    .collect(Collectors.toList());
        } catch (PatternSyntaxException ignored) {
            throw new InvalidRegexpException();
        } catch (IOException e) {
            log.error(Arrays.toString(e.getStackTrace()));

            throw new StorageServiceException();
        }

        return itemsMatchingRegexp;
    }
}
