package com.andreitraistaru.filestorage.utils;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Log4j2
public class FileOperations {
    public static boolean deleteRecursively(File file) {
        if (file == null) {
            return true;
        }

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

    public static long countNumberOfItemsInStorage(Path root) throws IOException {
        if (root == null || !Files.exists(root)) {
            return 0;
        }

        try (Stream<Path> filesWalking = Files.walk(root)) {
            return filesWalking.filter(path -> !Files.isDirectory(path)).count();
        }
    }
}
