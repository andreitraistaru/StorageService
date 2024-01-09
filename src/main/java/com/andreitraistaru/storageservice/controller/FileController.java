package com.andreitraistaru.storageservice.controller;

import com.andreitraistaru.storageservice.exception.AlreadyExistingStorageItemException;
import com.andreitraistaru.storageservice.exception.MissingStorageItemException;
import com.andreitraistaru.storageservice.service.CloudFileStorage;
import com.andreitraistaru.storageservice.service.FileStorageInterface;
import com.andreitraistaru.storageservice.service.LocalFileStorage;
import com.andreitraistaru.storageservice.utils.StorageType;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

@RestController
@RequestMapping("/file")
@Log4j2
@AllArgsConstructor
public class FileController {
    private final HashMap<StorageType, FileStorageInterface> storageServices = new HashMap<>();

    @Autowired
    public FileController(CloudFileStorage cloudFileStorage,
                          LocalFileStorage localFileStorage) {
        this.storageServices.put(StorageType.CLOUD, cloudFileStorage);
        this.storageServices.put(StorageType.LOCAL, localFileStorage);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createFile(@RequestParam("filename") String filename,
                                             @RequestParam("file") MultipartFile newFile,
                                             @RequestParam("storageType") StorageType storageType) {
        System.out.println("filename" + filename);
        System.out.println("file (name)" + newFile.getName());
        System.out.println("file (original name)" + newFile.getOriginalFilename());
        System.out.println("storageType" + storageType);

        if (!storageServices.containsKey(storageType)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            return new ResponseEntity<>(storageServices.get(storageType).createFile(filename, newFile), HttpStatus.CREATED);
        } catch (AlreadyExistingStorageItemException ignored) {
            return new ResponseEntity<>("File already existing", HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFile(@RequestParam("filename") String filename,
                                             @RequestParam("storageType") StorageType storageType) {
        System.out.println("filename" + filename);
        System.out.println("storageType" + storageType);

        try {
            if (!storageServices.containsKey(storageType)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            storageServices.get(storageType).deleteFile(filename);

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (MissingStorageItemException ignored) {
            return new ResponseEntity<>("File not existing.", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateFile(@RequestParam("filename") String filename,
                                             @RequestParam("file") MultipartFile updatedFile,
                                             @RequestParam("storageType") StorageType storageType) {
        System.out.println("filename" + filename);
        System.out.println("file (name)" + updatedFile.getName());
        System.out.println("file (original name)" + updatedFile.getOriginalFilename());
        System.out.println("storageType" + storageType);

        if (!storageServices.containsKey(storageType)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            return new ResponseEntity<>(storageServices.get(storageType).updateFile(filename, updatedFile), HttpStatus.OK);
        } catch (MissingStorageItemException ignored) {
            return new ResponseEntity<>("File not existing.", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/read")
    public ResponseEntity<?> readFile(@RequestParam("filename") String filename,
                                      @RequestParam(value = "version", required = false) String version,
                                      @RequestParam("storageType") StorageType storageType) {
        System.out.println("filename" + filename);
        System.out.println("version" + version);
        System.out.println("storageType" + storageType);

        if (!storageServices.containsKey(storageType)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Resource resource;

            resource = storageServices.get(storageType).downloadFile(filename, version);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (MissingStorageItemException ignored) {
            return new ResponseEntity<>("File not existing.", HttpStatus.NOT_FOUND);
        } catch (Throwable ignored) {
            return new ResponseEntity<>("Something went wrong. Try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
