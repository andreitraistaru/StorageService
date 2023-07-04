package com.andreitraistaru.filestorage.controller;

import com.andreitraistaru.filestorage.exception.AlreadyExistingStorageItemException;
import com.andreitraistaru.filestorage.exception.InvalidStorageItemNameException;
import com.andreitraistaru.filestorage.exception.MissingStorageItemException;
import com.andreitraistaru.filestorage.service.StorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    @Test
    @DisplayName("Read file bad request")
    void readFile_badRequest() throws Exception {
        Mockito.doThrow(InvalidStorageItemNameException.class)
                .when(storageService).readStorageItem(" ");

        Mockito.doThrow(InvalidStorageItemNameException.class)
                .when(storageService).readStorageItem("??");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/file/read"))
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/file/read")
                        .param("filename", " "))
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/file/read")
                        .param("filename", "??"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Read file not found")
    void readFile_notFound() throws Exception {
        Mockito.doThrow(MissingStorageItemException.class)
                .when(storageService).readStorageItem("test_file_non_existent");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/file/read")
                        .param("filename", "test_file_non_existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Read file ok")
    void readFile_ok() throws Exception {
        File file = org.assertj.core.util.Files.newTemporaryFile();
        Files.write(file.toPath(), "file_content".getBytes());
        assertTrue(file.isFile());
        assertArrayEquals("file_content".getBytes(), Files.readAllBytes(file.toPath()));

        Mockito.doReturn(new UrlResource(file.toURI()))
                .when(storageService).readStorageItem("test_file_existent");

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/file/read")
                        .param("filename", "test_file_existent"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().exists(HttpHeaders.CONTENT_DISPOSITION))
                .andExpect(content().bytes(Files.readAllBytes(file.toPath())))
                .andReturn();

        assertEquals("attachment; filename=\"test_file_existent\"",
                mvcResult.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
    }

    @Test
    @DisplayName("Create file bad request")
    void createFile_badRequest() throws Exception {
        Mockito.doThrow(InvalidStorageItemNameException.class)
                .when(storageService).createStorageItem("", null);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/file/create"))
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/file/create")
                        .param("filename", ""))
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/file/create")
                        .param("filename", "test_file"))
                .andExpect(status().isBadRequest());

        MockMultipartFile multipartFile_1 = new MockMultipartFile("file", "file_content".getBytes());

        Mockito.doThrow(InvalidStorageItemNameException.class)
                .when(storageService).createStorageItem("", multipartFile_1);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/file/create")
                        .file(multipartFile_1))
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/file/create")
                        .param("filename", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Create file conflict")
    void createFile_conflict() throws Exception {
        MockMultipartFile multipartFile_1 = new MockMultipartFile("file", "file_content".getBytes());

        Mockito.doThrow(AlreadyExistingStorageItemException.class)
                        .when(storageService).createStorageItem("test_file_conflict", multipartFile_1);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/file/create")
                        .file(multipartFile_1)
                        .param("filename", "test_file_conflict"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Create file created")
    void createFile_created() throws Exception {
        MockMultipartFile multipartFile_1 = new MockMultipartFile("file", "file_content".getBytes());

        Mockito.doNothing()
                .when(storageService).createStorageItem("test_file", multipartFile_1);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/file/create")
                        .file(multipartFile_1)
                        .param("filename", "test_file"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Update file bad request")
    void updateFile_badRequest() throws Exception {
        Mockito.doThrow(InvalidStorageItemNameException.class)
                .when(storageService).updateStorageItem("", null);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post("/file/update"))
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/file/update")
                        .param("filename", ""))
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/file/update")
                        .param("filename", "test_file"))
                .andExpect(status().isBadRequest());

        MockMultipartFile multipartFile_1 = new MockMultipartFile("file", "file_content".getBytes());

        Mockito.doThrow(InvalidStorageItemNameException.class)
                .when(storageService).updateStorageItem("", multipartFile_1);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/file/update")
                        .file(multipartFile_1))
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/file/update")
                        .param("filename", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Update file not found")
    void updateFile_notFound() throws Exception {
        MockMultipartFile multipartFile_1 = new MockMultipartFile("file", "file_content".getBytes());

        Mockito.doThrow(MissingStorageItemException.class)
                .when(storageService).updateStorageItem("test_file_non_existent", multipartFile_1);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/file/update")
                        .file(multipartFile_1)
                        .param("filename", "test_file_non_existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update file ok")
    void updateFile_updated() throws Exception {
        MockMultipartFile multipartFile_1 = new MockMultipartFile("file", "file_content".getBytes());

        Mockito.doNothing()
                .when(storageService).updateStorageItem("test_file_existent", multipartFile_1);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/file/update")
                        .file(multipartFile_1)
                        .param("filename", "test_file_existent"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Delete file bad request")
    void deleteFile_badRequest() throws Exception {
        Mockito.doThrow(InvalidStorageItemNameException.class)
                .when(storageService).deleteStorageItem(" ");

        Mockito.doThrow(InvalidStorageItemNameException.class)
                .when(storageService).deleteStorageItem("??");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/file/delete"))
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/file/delete")
                        .param("filename", " "))
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/file/delete")
                        .param("filename", "??"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Delete file not found")
    void deleteFile_notFound() throws Exception {
        Mockito.doThrow(MissingStorageItemException.class)
                .when(storageService).deleteStorageItem("test_file_non_existent");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/file/delete")
                        .param("filename", "test_file_non_existent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete file ok")
    void deleteFile_deleted() throws Exception {
        Mockito.doNothing()
                .when(storageService).deleteStorageItem("test_file_existent");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete("/file/delete")
                        .param("filename", "test_file_existent"))
                .andExpect(status().isOk());
    }
}
