package com.andreitraistaru.filestorage.controller;

import com.andreitraistaru.filestorage.dto.FilesMatchingRegexpDTO;
import com.andreitraistaru.filestorage.dto.NumberOfFilesDTO;
import com.andreitraistaru.filestorage.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
@RequestMapping("/storage")
public class StorageMetadataController {
    private final StorageService storageService;

    public StorageMetadataController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/match-filename")
    public ResponseEntity<FilesMatchingRegexpDTO> matchFilenameWithRegexp(@RequestParam("regexp") String regexp) {
        FilesMatchingRegexpDTO response = new FilesMatchingRegexpDTO();
        response.setRegexp(regexp);
        response.setFilenames(new ArrayList<>());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/number-of-files")
    public ResponseEntity<NumberOfFilesDTO> getNumberOfFilesInStorage() {
        NumberOfFilesDTO response = new NumberOfFilesDTO();
        response.setNumberOfFiles(storageService.getTotalNumberOfItemsInStorage());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
