package com.andreitraistaru.cloudfilestorage.controller;

import com.andreitraistaru.cloudfilestorage.service.S3ClientService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@Log4j2
@AllArgsConstructor
public class FileController {
    private final S3ClientService s3ClientService;

    @PostMapping("/create")
    public ResponseEntity<String> createFile(@RequestParam("filename") String filename,
                                             @RequestParam("file") MultipartFile newFile) {
        try {
            if (s3ClientService.checkFileExists(filename)) {
                return new ResponseEntity<>("File already existing.", HttpStatus.CONFLICT);
            }
        } catch(Throwable ignored) {
            return new ResponseEntity<>("Something went wrong. Try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String versionId;

        try {
            versionId = s3ClientService.uploadFile(filename, newFile);
        } catch(Throwable ignored) {
            return new ResponseEntity<>("Invalid file provided", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("File " + filename + " created successfully. VersionId: " + versionId, HttpStatus.CREATED);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam("filename") String filename) {
        try {
            if (!s3ClientService.checkFileExists(filename)) {
                return new ResponseEntity<>("File not existing.", HttpStatus.NOT_FOUND);
            }
        } catch(Throwable ignored) {
            return new ResponseEntity<>("Something went wrong. Try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        s3ClientService.deleteFile(filename);

        return new ResponseEntity<>("File " + filename + " deleted successfully.", HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateFile(@RequestParam("filename") String filename,
                                             @RequestParam("file") MultipartFile updatedFile) {
        try {
            if (!s3ClientService.checkFileExists(filename)) {
                return new ResponseEntity<>("File not existing.", HttpStatus.NOT_FOUND);
            }
        } catch(Throwable ignored) {
            return new ResponseEntity<>("Something went wrong. Try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        String versionId;

        try {
            versionId = s3ClientService.uploadFile(filename, updatedFile);
        } catch(Throwable ignored) {
            return new ResponseEntity<>("Invalid file provided", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("File " + filename + " updated successfully. VersionId: " + versionId, HttpStatus.OK);
    }

    @GetMapping("/read")
    public ResponseEntity<?> readFile(@RequestParam("filename") String filename,
                                      @RequestParam(value = "version", required = false) String version) {
        try {
            if (!s3ClientService.checkFileExists(filename)) {
                return new ResponseEntity<>("File not existing.", HttpStatus.NOT_FOUND);
            }
        } catch(Throwable ignored) {
            return new ResponseEntity<>("Something went wrong. Try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Resource resource;

        try {
            resource = s3ClientService.downloadFile(filename, version);
        } catch (Throwable e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
