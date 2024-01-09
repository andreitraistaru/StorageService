package com.andreitraistaru.storageservice.service;

import com.andreitraistaru.storageservice.dto.FilesMatchingRegexpDTO;
import com.andreitraistaru.storageservice.dto.NumberOfFilesDTO;
import com.andreitraistaru.storageservice.exception.AlreadyExistingStorageItemException;
import com.andreitraistaru.storageservice.exception.MissingStorageItemException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

@Service
public class LocalFileStorage implements FileStorageInterface {
    @Value("${versioning.service.url}")
    private String versioningServiceUrl;
    @Value("${path.calculator.service.url}")
    private String pathCalculatorServiceUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    private String computePathBasedOnFilename(String filename) {
        System.out.println("computePathBasedOnFilename: filename = " + filename);
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(pathCalculatorServiceUrl + "/get-filename")
                .queryParam("filename", filename);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String responseString = response.getBody();
                System.out.println("computePathBasedOnFilename: responseString = " + responseString);
                return responseString;
            }

            System.out.println("computePathBasedOnFilename: responseString = null");
            return null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public String createFile(String fileName, MultipartFile multipartFile) throws AlreadyExistingStorageItemException {
        System.out.println("createFile: fileName = " + fileName);
        String url = versioningServiceUrl + "/file/create";

        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();

        parts.add("filename", computePathBasedOnFilename(fileName));
        parts.add("file", multipartFile.getResource());
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts, httpHeaders);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, httpEntity, String.class);

            System.out.println("response body: " + response.getBody());
            System.out.println("response code: " + response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new AlreadyExistingStorageItemException();
            }
        } catch (Throwable ignored) {
            throw new AlreadyExistingStorageItemException();
        }
    }

    public String updateFile(String fileName, MultipartFile multipartFile) throws MissingStorageItemException {
        System.out.println("updateFile: fileName = " + fileName);
        String url = versioningServiceUrl + "/file/update";

        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();

        parts.add("filename", computePathBasedOnFilename(fileName));
        parts.add("file", multipartFile.getResource());
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts, httpHeaders);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, httpEntity, String.class);

            System.out.println("response body: " + response.getBody());
            System.out.println("response code: " + response.getStatusCode());

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new MissingStorageItemException();
            }
        } catch (Throwable ignored) {
            throw new MissingStorageItemException();
        }
    }

    public Resource downloadFile(String fileName, String version) throws MissingStorageItemException {
        System.out.println("downloadFile: fileName = " + fileName);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(versioningServiceUrl + "/file/read")
                .queryParam("filename", computePathBasedOnFilename(fileName))
                .queryParam("version", version);

        try {
            File file = restTemplate.execute(uriBuilder.toUriString(), HttpMethod.GET, null, clientHttpResponse -> {
                System.out.println("response body: " + clientHttpResponse.getBody());
                System.out.println("response code: " + clientHttpResponse.getStatusCode());

                if (!clientHttpResponse.getStatusCode().is2xxSuccessful()) {
                    return null;
                }

                File tmpFile = File.createTempFile("file_", ".tmp");

                StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(tmpFile));

                return tmpFile;
            });

            if (file == null) {
                System.out.println("response file: null");
                throw new MissingStorageItemException();
            }

            System.out.println("response file: " + file.getName());

            return new InputStreamResource(new FileInputStream(file));
        } catch (Throwable ignored) {
            throw new MissingStorageItemException();
        }
    }

    public void deleteFile(String fileName) throws MissingStorageItemException {
        System.out.println("deleteFile: fileName = " + fileName);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(versioningServiceUrl + "/file/delete")
                .queryParam("filename", computePathBasedOnFilename(fileName));

        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts, httpHeaders);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.DELETE, httpEntity, String.class);

            System.out.println("response body: " + response.getBody());
            System.out.println("response code: " + response.getStatusCode());

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new MissingStorageItemException();
            }
        } catch (Throwable ignored) {
            throw new MissingStorageItemException();
        }
    }

    public long getNumberOfFiles() {
        String url = versioningServiceUrl + "/storage/number-of-files";

        LinkedMultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        HttpHeaders httpHeaders = new HttpHeaders();

        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parts, httpHeaders);

        try {
            ResponseEntity<NumberOfFilesDTO> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, NumberOfFilesDTO.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody().getNumberOfFiles();
            }

            return -1;
        } catch (Throwable ignored) {
            return -1;
        }
    }

    public List<String> getFilesMatchingRegexp(String regexp) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(versioningServiceUrl + "/storage/match-filename")
                .queryParam("regexp", regexp);

        try {
            ResponseEntity<FilesMatchingRegexpDTO> response = restTemplate.exchange(uriBuilder.toUriString(), HttpMethod.GET, null, FilesMatchingRegexpDTO.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody().getFilenames();
            }

            return null;
        } catch (Throwable ignored) {
            return null;
        }
    }
}
