package com.andreitraistaru.filestorage.service;

import com.andreitraistaru.filestorage.exceptions.*;
import com.andreitraistaru.filestorage.utils.FileOperations;
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

import static com.andreitraistaru.filestorage.utils.FileOperations.countNumberOfItemsInStorage;
import static com.andreitraistaru.filestorage.utils.Validators.validateRegexp;
import static com.andreitraistaru.filestorage.utils.Validators.validateStorageItemName;

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

        this.numberOfStorageItems.set(countNumberOfItemsInStorage(root));
    }

    private File getFileForPersistenceForStorageItemName(String storageItemName) {
        // Builds up the path in the filesystem where the storage item will be saved
        StringBuilder rootPathForPersistence = new StringBuilder(rootPath);
        // Builds up the name of each folder from the storage hierarchy
        StringBuilder currentDirectoryName = new StringBuilder();
        int imbricationLevelsLeft = imbricationLevel;

        for (int i = 0; i < storageItemName.length(); i++) {
            // Building up the name of the directory from the current level of
            // imbrication where the file will finally land on
            currentDirectoryName.append(storageItemName.charAt(i));

            if (i % 2 == 1) {
                // We chose to split the name of the storage item in chunks
                // of two letters representing the name of the directory from
                // each level of imbrication
                rootPathForPersistence.append(currentDirectoryName).append("/");
                currentDirectoryName = new StringBuilder();
                imbricationLevelsLeft--;

                if (imbricationLevelsLeft == 0) {
                    // Maximum imbrication level reached; storing the file in
                    // the directory that we got so far
                    break;
                }
            }
        }

        // For better visibility and traceability, each storage item will be
        // saved with a `STORAGE_ITEM_EXTENSION` extension appended to its name
        rootPathForPersistence.append(storageItemName).append(STORAGE_ITEM_EXTENSION);

        return new File(rootPathForPersistence.toString());
    }

    private String getStorageItemNameFromFileName(String filename) {
        // Getting rid of the extension used to save the storage item in the
        // filesystem
        return filename.substring(0, filename.length() - STORAGE_ITEM_EXTENSION.length());
    }

    public boolean checkIfStorageItemExists(File storageItemFile) throws StorageCorruptionFoundException {
        if (storageItemFile.exists()) {
            if (storageItemFile.isDirectory()) {
                log.error("Storage service found a directory when searching for item at " +
                        storageItemFile.getAbsolutePath() + ". Storage might have been corrupted.");
                log.info("Recovering Storage started: ...");
                log.info("Deleting directory " + storageItemFile.getAbsolutePath());

                if (!FileOperations.deleteRecursively(storageItemFile)) {
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
        validateStorageItemName(storageItemName);

        File storageItemFileForPersistence = getFileForPersistenceForStorageItemName(storageItemName);
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
        validateStorageItemName(storageItemName);

        File storageItemFileForPersistence = getFileForPersistenceForStorageItemName(storageItemName);
        try {
            if (checkIfStorageItemExists(storageItemFileForPersistence)) {
                throw new AlreadyExistingStorageItemException();
            }
        } catch (StorageCorruptionFoundException e) {
            throw new StorageServiceException();
        }

        // Create directory hierarchy where the storage item will be saved
        storageItemFileForPersistence.getParentFile().mkdirs();

        // Saving the storage item
        try (OutputStream outputStream = new FileOutputStream(storageItemFileForPersistence)) {
            outputStream.write(storageItemMultipartFile.getBytes());
        } catch (IOException e) {
            log.error(Arrays.toString(e.getStackTrace()));

            throw new StorageServiceException();
        }

        // Updating the total number of files in the storage system
        numberOfStorageItems.incrementAndGet();
    }

    public void updateStorageItem(String storageItemName, MultipartFile storageItemMultipartFile) throws
            StorageServiceException {
        validateStorageItemName(storageItemName);

        File storageItemFileForPersistence = getFileForPersistenceForStorageItemName(storageItemName);
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

        // Rewriting the storage item in the filesystem
        try (OutputStream outputStream = new FileOutputStream(storageItemFileForPersistence)) {
            outputStream.write(storageItemMultipartFile.getBytes());
        } catch (IOException e) {
            log.error(Arrays.toString(e.getStackTrace()));

            throw new StorageServiceException();
        }
    }

    public void deleteStorageItem(String storageItemName) throws
            StorageCorruptionFoundException, MissingStorageItemException, InvalidStorageItemNameException {
        validateStorageItemName(storageItemName);

        File storageItemFileForPersistence = getFileForPersistenceForStorageItemName(storageItemName);
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

        // Delete the storage item from the filesystem
        if (!storageItemFileForPersistence.delete()) {
            throw new StorageCorruptionFoundException();
        }
        // Updating the total number of files in the storage system
        numberOfStorageItems.decrementAndGet();
    }

    public long getTotalNumberOfItemsInStorage() {
        return numberOfStorageItems.get();
    }

    public List<String> getStorageItemsMatchingRegexp(String regexp) throws StorageServiceException {
        validateRegexp(regexp);

        List<String> itemsMatchingRegexp;

        try (Stream<Path> filesWalking = Files.walk(Paths.get(rootPath))) {
            itemsMatchingRegexp = filesWalking
                    // Skipping directories
                    .filter(path -> !Files.isDirectory(path))
                    // Mapping each file's `Path` to the storage item name
                    .map(path -> getStorageItemNameFromFileName(path.getFileName().toString()))
                    // Filtering out non-matching strings with the regexp
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
