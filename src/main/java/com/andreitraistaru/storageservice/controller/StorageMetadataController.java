package com.andreitraistaru.storageservice.controller;

import com.andreitraistaru.storageservice.dto.FilesMatchingRegexpDTO;
import com.andreitraistaru.storageservice.dto.NumberOfFilesDTO;
import com.andreitraistaru.storageservice.service.CloudFileStorage;
import com.andreitraistaru.storageservice.service.FileStorageInterface;
import com.andreitraistaru.storageservice.service.LocalFileStorage;
import com.andreitraistaru.storageservice.utils.StorageType;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@RequestMapping("/storage")
@AllArgsConstructor
public class StorageMetadataController {
    private final HashMap<StorageType, FileStorageInterface> storageServices = new HashMap<>();

    @Autowired
    public StorageMetadataController(CloudFileStorage cloudFileStorage,
                                     LocalFileStorage localFileStorage) {
        this.storageServices.put(StorageType.CLOUD, cloudFileStorage);
        this.storageServices.put(StorageType.LOCAL, localFileStorage);
    }

    @GetMapping("/match-filename")
    public ResponseEntity<FilesMatchingRegexpDTO> matchFilenameWithRegexp(@RequestParam("regexp") String regexp,
                                                                          @RequestParam("storageType") StorageType storageType) {
        if (!storageServices.containsKey(storageType)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        FilesMatchingRegexpDTO response = new FilesMatchingRegexpDTO();
        response.setRegexp(regexp);
        response.setFilenames(storageServices.get(storageType).getFilesMatchingRegexp(regexp));

        if (response.getFilenames() == null) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/number-of-files")
    public ResponseEntity<NumberOfFilesDTO> getNumberOfFilesInStorage(@RequestParam("storageType") StorageType storageType) {
        if (!storageServices.containsKey(storageType)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        NumberOfFilesDTO response = new NumberOfFilesDTO();

        response.setNumberOfFiles(storageServices.get(storageType).getNumberOfFiles());

        if (response.getNumberOfFiles() == -1) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
