package com.andreitraistaru.cloudfilestorage.controller;

import com.andreitraistaru.cloudfilestorage.dto.FilesMatchingRegexpDTO;
import com.andreitraistaru.cloudfilestorage.dto.NumberOfFilesDTO;
import com.andreitraistaru.cloudfilestorage.service.S3ClientService;
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
    private final S3ClientService s3ClientService;

    @GetMapping("/match-filename")
    public ResponseEntity<FilesMatchingRegexpDTO> matchFilenameWithRegexp(@RequestParam("regexp") String regexp) {
        FilesMatchingRegexpDTO response = new FilesMatchingRegexpDTO();
        response.setRegexp(regexp);

        try {
            response.setFilenames(s3ClientService.listFiles()
                    .stream()
                    .filter(filename -> filename.matches(regexp))
                    .toList());
        } catch(Throwable ignored) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/number-of-files")
    public ResponseEntity<NumberOfFilesDTO> getNumberOfFilesInStorage() {
        NumberOfFilesDTO response = new NumberOfFilesDTO();

        try {
            response.setNumberOfFiles(s3ClientService.listFiles().size());
        } catch(Throwable ignored) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
