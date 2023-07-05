package com.andreitraistaru.filestorage.service;

import com.andreitraistaru.filestorage.exception.*;
import com.andreitraistaru.filestorage.utils.FileOperations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class StorageServiceTests {
    StorageService storageService;
    Path root;

    @BeforeEach
    void init() throws IOException {
        this.root = Files.createTempDirectory(null);
        this.storageService = new StorageService(this.root.toString(), 3);
    }

    @AfterEach
    void cleanup() {
        FileOperations.deleteRecursively(this.root.toFile());

        this.root = null;
        this.storageService = null;
    }

    @Test
    @DisplayName("Storage service actions with invalid item name")
    void storageService_invalidStorageItemName() {
        // Create Storage Item
        MockMultipartFile multipartFileMock_1 = new MockMultipartFile("a??b", "".getBytes());
        assertThrows(InvalidStorageItemNameException.class, () -> storageService.createStorageItem("a??b", multipartFileMock_1));
        // This is `—` instead of `-` so we expect to get an InvalidStorageItemNameException for it
        MockMultipartFile multipartFileMock_2 = new MockMultipartFile("a—b", "".getBytes());
        assertThrows(InvalidStorageItemNameException.class, () -> storageService.createStorageItem("a—b", multipartFileMock_2));

        // Read Storage Item
        assertThrows(InvalidStorageItemNameException.class, () -> storageService.readStorageItem("a??b"));
        // This is `—` instead of `-` so we expect to get an InvalidStorageItemNameException for it
        assertThrows(InvalidStorageItemNameException.class, () -> storageService.readStorageItem("a—b"));

        // Delete Storage Item
        assertThrows(InvalidStorageItemNameException.class, () -> storageService.deleteStorageItem("a??b"));
        // This is `—` instead of `-` so we expect to get an InvalidStorageItemNameException for it
        assertThrows(InvalidStorageItemNameException.class, () -> storageService.deleteStorageItem("a—b"));

        // Update Storage Item
        MockMultipartFile multipartFileMock_3 = new MockMultipartFile("a??b", "".getBytes());
        assertThrows(InvalidStorageItemNameException.class, () -> storageService.updateStorageItem("a??b", multipartFileMock_3));
        // This is `—` instead of `-` so we expect to get an InvalidStorageItemNameException for it
        MockMultipartFile multipartFileMock_4 = new MockMultipartFile("a—b", "".getBytes());
        assertThrows(InvalidStorageItemNameException.class, () -> storageService.updateStorageItem("a—b", multipartFileMock_4));
    }

    @Test
    @DisplayName("Create storage item")
    void createStorageItem() throws StorageServiceException, IOException {
        MockMultipartFile multipartFileMock_1 = new MockMultipartFile("test_file_1", "".getBytes());
        storageService.createStorageItem("test_file_1", multipartFileMock_1);
        File file_1 = Paths.get(root.toString(), "te", "st", "_f", "test_file_1.storage").toFile();
        assertTrue(file_1.isFile());
        assertArrayEquals("".getBytes(), Files.readAllBytes(file_1.toPath()));

        assertThrows(AlreadyExistingStorageItemException.class, () -> storageService.createStorageItem("test_file_1", multipartFileMock_1));

        storageService.createStorageItem("test_file_2", multipartFileMock_1);
        File file_2 = Paths.get(root.toString(), "te", "st", "_f", "test_file_2.storage").toFile();
        assertTrue(file_2.isFile());
        assertArrayEquals("".getBytes(), Files.readAllBytes(file_2.toPath()));

        MultipartFile multipartFileMock_2 = new MockMultipartFile("foo", "foo_content".getBytes());
        storageService.createStorageItem("foo", multipartFileMock_2);
        File file_3 = Paths.get(root.toString(), "fo", "foo.storage").toFile();
        assertTrue(file_3.isFile());
        assertArrayEquals("foo_content".getBytes(), Files.readAllBytes(file_3.toPath()));
    }

    @Test
    @DisplayName("Delete storage item")
    void deleteStorageItem() throws StorageServiceException, IOException {
        File file_1 = Paths.get(root.toString(), "te", "st", "_f", "test_file_1.storage").toFile();
        file_1.getParentFile().mkdirs();
        file_1.createNewFile();
        assertTrue(file_1.isFile());
        storageService.deleteStorageItem("test_file_1");
        assertFalse(file_1.isFile());

        File file_2 = Paths.get(root.toString(), "fo", "fo.storage").toFile();
        File file_3 = Paths.get(root.toString(), "fo", "foo.storage").toFile();
        file_2.getParentFile().mkdirs();
        file_2.createNewFile();
        file_3.getParentFile().mkdirs();
        file_3.createNewFile();
        assertTrue(file_2.isFile());
        assertTrue(file_3.isFile());
        storageService.deleteStorageItem("foo");
        assertTrue(file_2.isFile());
        assertFalse(file_3.isFile());
        assertThrows(MissingStorageItemException.class, () -> storageService.deleteStorageItem("foo"));
        storageService.deleteStorageItem("fo");
        assertFalse(file_2.isFile());
        assertFalse(file_3.isFile());
    }

    @Test
    @DisplayName("Read storage item")
    void readStorageItem() throws StorageServiceException, IOException {
        assertThrows(MissingStorageItemException.class, () -> storageService.readStorageItem("test_file_1"));

        File file_1 = Paths.get(root.toString(), "te", "st", "_f", "test_file_1.storage").toFile();
        file_1.getParentFile().mkdirs();
        file_1.createNewFile();
        assertTrue(file_1.isFile());
        assertArrayEquals("".getBytes(), storageService.readStorageItem("test_file_1").getContentAsByteArray());

        File file_2 = Paths.get(root.toString(), "fo", "fo.storage").toFile();
        File file_3 = Paths.get(root.toString(), "fo", "foo.storage").toFile();
        file_2.getParentFile().mkdirs();
        file_2.createNewFile();
        Files.write(file_2.toPath(), "fo".getBytes());
        file_3.getParentFile().mkdirs();
        file_3.createNewFile();
        Files.write(file_3.toPath(), "foo".getBytes());
        assertTrue(file_2.isFile());
        assertArrayEquals("fo".getBytes(), Files.readAllBytes(file_2.toPath()));
        assertTrue(file_3.isFile());
        assertArrayEquals("foo".getBytes(), Files.readAllBytes(file_3.toPath()));
        assertArrayEquals("fo".getBytes(), storageService.readStorageItem("fo").getContentAsByteArray());
        assertArrayEquals("foo".getBytes(), storageService.readStorageItem("foo").getContentAsByteArray());
    }

    @Test
    @DisplayName("Update storage item")
    void updateStorageItem() throws StorageServiceException, IOException {
        MockMultipartFile multipartFileMock_1 = new MockMultipartFile("test_file_1", "".getBytes());
        assertThrows(MissingStorageItemException.class, () -> storageService.updateStorageItem("test_file_1", multipartFileMock_1));

        File file_1 = Paths.get(root.toString(), "fo", "foo.storage").toFile();
        file_1.getParentFile().mkdirs();
        file_1.createNewFile();
        Files.write(file_1.toPath(), "foo_content".getBytes());
        assertTrue(file_1.isFile());
        assertArrayEquals("foo_content".getBytes(), Files.readAllBytes(file_1.toPath()));
        MultipartFile multipartFileMock_2 = new MockMultipartFile("foo", "foo_content_modified".getBytes());
        storageService.updateStorageItem("foo", multipartFileMock_2);
        assertTrue(file_1.isFile());
        assertArrayEquals("foo_content_modified".getBytes(), Files.readAllBytes(file_1.toPath()));

        File file_2 = Paths.get(root.toString(), "ba", "bar.storage").toFile();
        file_2.getParentFile().mkdirs();
        file_2.createNewFile();
        Files.write(file_2.toPath(), "bar_content".getBytes());
        assertTrue(file_2.isFile());
        assertArrayEquals("bar_content".getBytes(), Files.readAllBytes(file_2.toPath()));
        MultipartFile multipartFileMock_3 = new MockMultipartFile("bar", "bar_content_modified".getBytes());
        storageService.updateStorageItem("bar", multipartFileMock_3);
        assertTrue(file_2.isFile());
        assertArrayEquals("bar_content_modified".getBytes(), Files.readAllBytes(file_2.toPath()));
        assertTrue(file_1.isFile());
        assertArrayEquals("foo_content_modified".getBytes(), Files.readAllBytes(file_1.toPath()));
        MultipartFile multipartFileMock_4 = new MockMultipartFile("foo", "foo_content_modified_twice".getBytes());
        storageService.updateStorageItem("foo", multipartFileMock_4);
        assertTrue(file_2.isFile());
        assertArrayEquals("bar_content_modified".getBytes(), Files.readAllBytes(file_2.toPath()));
        assertTrue(file_1.isFile());
        assertArrayEquals("foo_content_modified_twice".getBytes(), Files.readAllBytes(file_1.toPath()));
    }

    @Test
    @DisplayName("Total number of storage item")
    void getTotalNumberOfItemsInStorage() throws StorageServiceException, IOException {
        assertEquals(0, storageService.getTotalNumberOfItemsInStorage());

        MultipartFile multipartFile_1 = new MockMultipartFile("test", "".getBytes());
        storageService.createStorageItem("test", multipartFile_1);

        assertEquals(1, storageService.getTotalNumberOfItemsInStorage());

        File file_1 = Paths.get(root.toString(), "te", "st", "_f", "test_file_1.storage").toFile();
        file_1.getParentFile().mkdirs();
        file_1.createNewFile();
        assertTrue(file_1.isFile());

        assertEquals(1, storageService.getTotalNumberOfItemsInStorage());

        File file_2 = Paths.get(root.toString(), "fo", "fo.storage").toFile();
        file_2.getParentFile().mkdirs();
        file_2.createNewFile();
        assertTrue(file_2.isFile());

        File file_3= Paths.get(root.toString(), "fo", "foo.storage").toFile();
        file_3.getParentFile().mkdirs();
        file_3.createNewFile();
        assertTrue(file_3.isFile());

        File file_4= Paths.get(root.toString(), "fo", "ob", "foobar.storage").toFile();
        file_4.getParentFile().mkdirs();
        file_4.createNewFile();
        assertTrue(file_4.isFile());

        storageService = new StorageService(root.toString(), 3);

        assertEquals(5, storageService.getTotalNumberOfItemsInStorage());
    }

    @Test
    @DisplayName("Items matching regexp")
    void getStorageItemsMatchingRegexp() throws StorageServiceException, IOException {
        assertThrows(InvalidRegexpException.class, () -> storageService.getStorageItemsMatchingRegexp("**"));

        assertThat(List.of()).hasSameElementsAs(storageService.getStorageItemsMatchingRegexp(".+"));

        MultipartFile multipartFile_1 = new MockMultipartFile("test", "".getBytes());
        storageService.createStorageItem("test", multipartFile_1);

        assertThat(List.of("test")).hasSameElementsAs(storageService.getStorageItemsMatchingRegexp(".+"));

        File file_1 = Paths.get(root.toString(), "te", "st", "_f", "test_file_1.storage").toFile();
        file_1.getParentFile().mkdirs();
        file_1.createNewFile();
        assertTrue(file_1.isFile());

        assertThat(List.of("test", "test_file_1")).hasSameElementsAs(storageService.getStorageItemsMatchingRegexp(".+"));

        File file_2 = Paths.get(root.toString(), "fo", "fo.storage").toFile();
        file_2.getParentFile().mkdirs();
        file_2.createNewFile();
        assertTrue(file_2.isFile());

        assertThat(List.of("test", "test_file_1", "fo")).hasSameElementsAs(storageService.getStorageItemsMatchingRegexp(".+"));

        File file_3= Paths.get(root.toString(), "fo", "foo.storage").toFile();
        file_3.getParentFile().mkdirs();
        file_3.createNewFile();
        assertTrue(file_3.isFile());

        assertThat(List.of("test", "test_file_1", "fo", "foo")).hasSameElementsAs(storageService.getStorageItemsMatchingRegexp(".+"));

        File file_4= Paths.get(root.toString(), "fo", "ob", "foobar.storage").toFile();
        file_4.getParentFile().mkdirs();
        file_4.createNewFile();
        assertTrue(file_4.isFile());

        assertThat(List.of("test", "test_file_1", "fo", "foo", "foobar")).hasSameElementsAs(storageService.getStorageItemsMatchingRegexp(".+"));
        assertThat(List.of("test", "test_file_1")).hasSameElementsAs(storageService.getStorageItemsMatchingRegexp(".+est.*"));
        assertThat(List.of("test_file_1")).hasSameElementsAs(storageService.getStorageItemsMatchingRegexp(".+est.+"));
        assertThat(List.of("foo")).hasSameElementsAs(storageService.getStorageItemsMatchingRegexp(".*oo"));
        assertThat(List.of("test", "test_file_1", "fo", "foo", "foobar")).hasSameElementsAs(storageService.getStorageItemsMatchingRegexp(".*.*.*"));
    }
}
