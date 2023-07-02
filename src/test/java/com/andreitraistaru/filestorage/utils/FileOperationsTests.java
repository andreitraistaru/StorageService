package com.andreitraistaru.filestorage.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class FileOperationsTests {
    @Test
    @DisplayName("Delete recursively null file")
    void deleteRecursively_nullFile() {
        assertTrue(FileOperations.deleteRecursively(null));
    }

    @Test
    @DisplayName("Delete recursively non existent file")
    void deleteRecursively_nonExistentFile() throws IOException {
        Path filePath = Files.createTempFile(null, null);
        Files.delete(filePath);

        assertTrue(FileOperations.deleteRecursively(filePath.toFile()));
    }

    @Test
    @DisplayName("Delete recursively existent file")
    void deleteRecursively_existentFile() throws IOException {
        Path filePath = Files.createTempFile(null, null);

        assertTrue(Files.exists(filePath));

        assertTrue(FileOperations.deleteRecursively(filePath.toFile()));

        assertFalse(Files.exists(filePath));
    }

    @Test
    @DisplayName("Delete recursively existent empty directory")
    void deleteRecursively_existentEmptyDirectory() throws IOException {
        Path directoryPath = Files.createTempDirectory(null);

        assertTrue(Files.exists(directoryPath));

        assertTrue(FileOperations.deleteRecursively(directoryPath.toFile()));

        assertFalse(Files.exists(directoryPath));
    }

    @Test
    @DisplayName("Delete recursively existent directory containing files")
    void deleteRecursively_existentDirectoryContainingFiles() throws IOException {
        Path directoryPath = Files.createTempDirectory(null);
        Path filePath = Files.createTempFile(directoryPath, null, null);

        assertTrue(Files.exists(filePath));
        assertTrue(Files.exists(directoryPath));

        assertTrue(FileOperations.deleteRecursively(directoryPath.toFile()));

        assertFalse(Files.exists(filePath));
        assertFalse(Files.exists(directoryPath));
    }

    @Test
    @DisplayName("Delete recursively existent directory containing files and subdirectories")
    void deleteRecursively_existentDirectoryContainingFilesAndDirectories() throws IOException {
        Path directoryPath = Files.createTempDirectory(null);
        Path subdirectoryPath = Files.createTempDirectory(directoryPath, null);
        Path filePath = Files.createTempFile(subdirectoryPath, null, null);

        assertTrue(Files.exists(filePath));
        assertTrue(Files.exists(subdirectoryPath));
        assertTrue(Files.exists(directoryPath));

        assertTrue(FileOperations.deleteRecursively(directoryPath.toFile()));

        assertFalse(Files.exists(filePath));
        assertFalse(Files.exists(subdirectoryPath));
        assertFalse(Files.exists(directoryPath));
    }

    @Test
    @DisplayName("Number of items in storage null root provided")
    void countNumberOfItemsInStorage_nullStorage() throws IOException {
        assertEquals(0, FileOperations.countNumberOfItemsInStorage(null));
    }

    @Test
    @DisplayName("Number of items in non existing storage")
    void countNumberOfItemsInStorage_nonExistingStorage() throws IOException {
        Path directoryPath = Files.createTempDirectory(null);
        Files.delete(directoryPath);

        assertEquals(0, FileOperations.countNumberOfItemsInStorage(directoryPath));
    }

    @Test
    @DisplayName("Number of items in empty storage")
    void countNumberOfItemsInStorage_emptyStorage() throws IOException {
        Path directoryPath = Files.createTempDirectory(null);

        assertTrue(Files.exists(directoryPath));
        assertEquals(0, FileOperations.countNumberOfItemsInStorage(directoryPath));

        Path subdirectoryPath = Files.createTempDirectory(directoryPath, null);

        assertTrue(Files.exists(subdirectoryPath));
        assertEquals(0, FileOperations.countNumberOfItemsInStorage(directoryPath));
    }

    @Test
    @DisplayName("Number of items in populated storage")
    void countNumberOfItemsInStorage_populatedStorage() throws IOException {
        Path directoryPath = Files.createTempDirectory(null);
        Path subdirectoryPath = Files.createTempDirectory(directoryPath, null);
        Path filePath_1 = Files.createTempFile(subdirectoryPath, null, null);

        assertTrue(Files.exists(filePath_1));
        assertTrue(Files.exists(subdirectoryPath));
        assertTrue(Files.exists(directoryPath));

        assertEquals(1, FileOperations.countNumberOfItemsInStorage(directoryPath));

        Path filePath_2 = Files.createTempFile(subdirectoryPath, null, null);

        assertTrue(Files.exists(filePath_2));
        assertEquals(2, FileOperations.countNumberOfItemsInStorage(directoryPath));

        Path filePath_3 = Files.createTempFile(directoryPath, null, null);

        assertTrue(Files.exists(filePath_3));
        assertEquals(3, FileOperations.countNumberOfItemsInStorage(directoryPath));

        FileOperations.deleteRecursively(subdirectoryPath.toFile());

        assertEquals(1, FileOperations.countNumberOfItemsInStorage(directoryPath));
    }
}
