package com.andreitraistaru.filestorage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
public class FileController {
    @PostMapping("/create")
    public ResponseEntity<String> createFile(@RequestParam("filename") String filename,
                                             @RequestParam("file") MultipartFile newFile) {
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
