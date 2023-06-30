package com.andreitraistaru.filestorage.controller;

import com.andreitraistaru.filestorage.exceptions.AlreadyExistingStorageItemException;
import com.andreitraistaru.filestorage.exceptions.StorageCorruptionFoundException;
import com.andreitraistaru.filestorage.service.StorageService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@RestController
@RequestMapping("/file")
@Log4j2
public class FileController {
    private final StorageService storageService;

    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createFile(@RequestParam("filename") String filename,
                                             @RequestParam("file") MultipartFile newFile) {
        try {
            storageService.createStorageItem(filename, newFile.getBytes());
        } catch(AlreadyExistingStorageItemException ignored) {
            return new ResponseEntity<>("File " + filename + " already existing. Storage system " +
                    "has not been modified. Try again using /update endpoint.", HttpStatus.CONFLICT);
        } catch (StorageCorruptionFoundException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(Arrays.toString(e.getStackTrace()));

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("File " + filename + " created successfully.", HttpStatus.CREATED);
    }

    @GetMapping("/read")
    public ResponseEntity<MultipartFile> readFile(@RequestParam("filename") String filename) {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateFile(@RequestParam("filename") String filename,
                                             @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>("File " + filename + " updated successfully.", HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam("filename") String filename) {
        return new ResponseEntity<>("File " + filename + " deleted successfully.", HttpStatus.OK);
    }
}
