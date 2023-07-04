package com.andreitraistaru.filestorage.controller;

import com.andreitraistaru.filestorage.dto.FilesMatchingRegexpDTO;
import com.andreitraistaru.filestorage.dto.NumberOfFilesDTO;
import com.andreitraistaru.filestorage.exception.InvalidRegexpException;
import com.andreitraistaru.filestorage.service.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StorageMetadataControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    private final ObjectMapper objectMapper;

    protected StorageMetadataControllerTests() {
        this.objectMapper = new ObjectMapper();
    }

    private <T> T fromJsonString(String jsonString, Class<T> objectClass) throws JsonProcessingException {
        return objectMapper.readValue(jsonString, objectClass);
    }

    @Test
    @DisplayName("Get number of files ok")
    void getNumberOfFilesInStorage_ok() throws Exception {
        NumberOfFilesDTO response = new NumberOfFilesDTO();
        response.setNumberOfFiles(5);

        Mockito.doReturn(response.getNumberOfFiles())
                .when(storageService).getTotalNumberOfItemsInStorage();

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/storage/number-of-files"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(response.getNumberOfFiles(), fromJsonString(mvcResult.getResponse().getContentAsString(), NumberOfFilesDTO.class).getNumberOfFiles());
    }

    @Test
    @DisplayName("Get files matching regexp bad request")
    void matchFilenameWithRegexp_badRequest() throws Exception {
        Mockito.doThrow(InvalidRegexpException.class)
                .when(storageService).getStorageItemsMatchingRegexp("**");

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/storage/match-filename"))
                .andExpect(status().isBadRequest());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/storage/match-filename")
                        .param("regexp", "**"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Get files matching regexp ok")
    void matchFilenameWithRegexp_ok() throws Exception {
        FilesMatchingRegexpDTO response = new FilesMatchingRegexpDTO();
        response.setRegexp(".*est.*");
        response.setFilenames(List.of("test", "est", "tested"));

        Mockito.doReturn(response.getFilenames())
                .when(storageService).getStorageItemsMatchingRegexp(response.getRegexp());

        MvcResult mvcResult = this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/storage/match-filename")
                        .param("regexp", ".*est.*"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        assertEquals(response.getRegexp(), fromJsonString(mvcResult.getResponse().getContentAsString(), FilesMatchingRegexpDTO.class).getRegexp());
        assertThat(response.getFilenames()).hasSameElementsAs(fromJsonString(mvcResult.getResponse().getContentAsString(), FilesMatchingRegexpDTO.class).getFilenames());
    }
}
