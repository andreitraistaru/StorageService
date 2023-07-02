package com.andreitraistaru.filestorage.controller;

import com.andreitraistaru.filestorage.dto.FilesMatchingRegexpDTO;
import com.andreitraistaru.filestorage.dto.NumberOfFilesDTO;
import com.andreitraistaru.filestorage.exceptions.InvalidRegexpException;
import com.andreitraistaru.filestorage.exceptions.StorageServiceException;
import com.andreitraistaru.filestorage.service.StorageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/storage")
@AllArgsConstructor
public class StorageMetadataController {
    private final StorageService storageService;

    @GetMapping("/match-filename")
    public ResponseEntity<FilesMatchingRegexpDTO> matchFilenameWithRegexp(@RequestParam("regexp") String regexp) {
        FilesMatchingRegexpDTO response = new FilesMatchingRegexpDTO();
        response.setRegexp(regexp);

        try {
            response.setFilenames(storageService.getStorageItemsMatchingRegexp(regexp));

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (InvalidRegexpException e) {
            response.setFilenames(null);

            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (StorageServiceException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/number-of-files")
    public ResponseEntity<NumberOfFilesDTO> getNumberOfFilesInStorage() {
        NumberOfFilesDTO response = new NumberOfFilesDTO();
        response.setNumberOfFiles(storageService.getTotalNumberOfItemsInStorage());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
